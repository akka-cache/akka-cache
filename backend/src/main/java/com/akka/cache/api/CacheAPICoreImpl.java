package com.akka.cache.api;

import akka.Done;
import akka.http.javadsl.model.*;
import akka.japi.Pair;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import akka.util.ByteStringBuilder;
import com.akka.cache.application.*;
import com.akka.cache.domain.Cache;
import com.akka.cache.domain.CacheInternalGetResponse;
import com.akka.cache.domain.CacheName;
import com.akka.cache.domain.PayloadChunk;
import com.akka.cache.streams.Chunker;
import com.akka.cache.utils.FutureHelper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import com.akka.cache.domain.CacheAPI.*;

public class CacheAPICoreImpl {
    private static final Logger log = LoggerFactory.getLogger(CacheAPICoreImpl.class);

    protected final static ContentType BINARY_PAYLOAD = ContentTypes.create(MediaTypes.APPLICATION_OCTET_STREAM);

    protected final ComponentClient componentClient;
    protected final boolean cacheNameNeededFirst;
    protected final TimerScheduler timerScheduler;
    protected final Optional<Duration> defaultTTL;
    protected final Materializer materializer;
    protected final int streamChunkParallelism;
    protected final long maxPayloadSize;

    public CacheAPICoreImpl(Config config, ComponentClient componentClient, TimerScheduler timerScheduler, Materializer materializer) {
        this.componentClient = componentClient;
        this.timerScheduler = timerScheduler;
        this.materializer = materializer;
        this.streamChunkParallelism = config.getInt("app.stream-chunk-parallelism");
        this.maxPayloadSize = config.getLong("app.stream-max-payload-size");
        cacheNameNeededFirst = config.getBoolean("app.cache-name-needed-first");
        if (config.hasPath("app.default-default-ttl")) {
            defaultTTL = Optional.of(config.getDuration("app.default-default-ttl"));
        } else {
            defaultTTL = Optional.empty();
        }
    }

    // Cache Names -- BEGIN
    public CompletionStage<HttpResponse> createCacheName(CacheNameRequest request) {
        CacheName cn = new CacheName(request.cacheName(), Optional.of(request.description()));
        return componentClient.forEventSourcedEntity(cn.cacheName())
                .method(CacheNameEntity::create)
                .invokeAsync(cn)
                .thenApply(__ -> HttpResponses.created());
    }
    
    public CompletionStage<HttpResponse> updateCacheName(CacheNameRequest request) {
        CacheName cn = new CacheName(request.cacheName(), Optional.of(request.description()));
        return componentClient.forEventSourcedEntity(cn.cacheName())
                .method(CacheNameEntity::update)
                .invokeAsync(cn)
                .thenApply(__ -> HttpResponses.accepted());
    }
    
    public CompletionStage<CacheName> getCacheName(String cacheName) {
        return componentClient.forEventSourcedEntity(cacheName)
                .method(CacheNameEntity::get)
                .invokeAsync()
                .thenApply(c -> c);
    }

    public CompletionStage<CacheView.CacheSummaries> getCacheKeyList(String cacheName) {
        return componentClient.forView()
                .method(CacheView::getCacheSummaries)
                .invokeAsync(cacheName)
                .exceptionally(ex -> {
                    throw HttpException.badRequest("No keys items found for ".concat(cacheName));
                });
    }

    // This deletes the cacheName as well as all the keys
    public CompletionStage<HttpResponse> deleteCacheKeys(String cacheName) {
        var startDeletionSetup = new CacheNameDeleteWorkflow.StartDeletionsSetup(cacheName, false);
        return componentClient.forWorkflow(cacheName)
                .method(CacheNameDeleteWorkflow::startDeletions)
                .invokeAsync(startDeletionSetup)
                .thenApply(transferState -> HttpResponses.accepted());
    }

    // This deletes all the cached data but leaves the cacheName in place
    public CompletionStage<HttpResponse> flushCacheKeys(String cacheName) {
        var startDeletionSetup = new CacheNameDeleteWorkflow.StartDeletionsSetup(cacheName, true);
        return componentClient.forWorkflow(cacheName)
                .method(CacheNameDeleteWorkflow::startDeletions)
                .invokeAsync(startDeletionSetup)
                .thenApply(transferState -> HttpResponses.accepted());
    }


    // Cache Names -- END

    // Cache API -- BEGIN

    private CompletionStage<Done> scheduleTTLTimerIfNeeded(String cacheName, String key, Optional<Duration> requestTTLSeconds) {
        Optional<Duration> ttlSeconds = Optional.empty();
        String cacheId = cacheName.concat(key);
        if (requestTTLSeconds.isPresent()) {
            ttlSeconds = requestTTLSeconds;
        }
        else if (defaultTTL.isPresent()) {
            ttlSeconds = defaultTTL;
        }
        if (ttlSeconds.isPresent()) {
            if (log.isDebugEnabled()) {
                log.debug("starting TTL timer for cacheName {} key {} at {} seconds.", cacheName, key, ttlSeconds.get().getSeconds());
            }
            return timerScheduler.startSingleTimer(
                    "keys".concat(cacheId),
                    ttlSeconds.get(),
                    componentClient.forTimedAction()
                            .method(CacheTimedAction::expireCacheTTL)
                            .deferred(cacheId)
            );
        }
        else {
            return CompletableFuture.completedFuture(Done.done());
        }

    }

    private CompletionStage<HttpResponse> createCacheEntity(String cacheName, String key, Cache cache) {
        String cacheId = cacheName.concat(key);
        var setResult = componentClient.forEventSourcedEntity(cacheId)
                .method(CacheEntity::set)
                .invokeAsync(cache)
                .thenApply(result -> HttpResponses.created());

        return setResult.thenCompose(result -> scheduleTTLTimerIfNeeded(cacheName, key, cache.ttlSeconds())
                .thenApply(__ -> result));
    }

    private Cache createSmallCacheObject(CacheRequest cacheRequest) {
        Optional<Duration> requestTTL = cacheRequest.ttlSeconds().map(Duration::ofSeconds);
        List<PayloadChunk> chunks = new ArrayList<>(List.of(new PayloadChunk(0, cacheRequest.value())));
        return new Cache(cacheRequest.org(), cacheRequest.cacheName(), cacheRequest.key(), requestTTL, false, cacheRequest.value().length, false, chunks);
    }

    private CompletionStage<HttpResponse> isCacheNameNeededFirst(String cacheName) {
        if (cacheNameNeededFirst) {
            CompletionStage<CacheName> cName = getCacheName(cacheName);
            return cName.thenCompose(c -> {
                if (c.cacheName().isEmpty()) {
                    return CompletableFuture.completedFuture(HttpResponses.notFound());
                }
                else {
                    return CompletableFuture.completedFuture(HttpResponses.accepted());
                }
            });
        } else {
            return CompletableFuture.completedFuture(HttpResponses.accepted());
        }
    }

    // large object so we need to chunk it up to the entity
    private CompletionStage<Done> streamLargeObjectAsChunks(CacheRequest cacheRequest) {
        String cacheId = cacheRequest.cacheName().concat(cacheRequest.key());
        int objectSize = cacheRequest.value().length;
        long chunks = objectSize / maxPayloadSize;
        long remainder = objectSize % maxPayloadSize;
        if (remainder > 0) chunks++;
        int chunkSize = (int) (objectSize / chunks + 1); // just assume a fraction and bump

        if (log.isDebugEnabled()) {
            log.debug("streamLargeObjectAsChunks object size {} chunks {}, chunksize {}", objectSize, chunks, chunkSize);
        }

        ByteString valueBytes = new ByteStringBuilder().putBytes(cacheRequest.value()).result();
        return Source.single(valueBytes)
                .via(new Chunker(chunkSize))
                .statefulMap(
                        () -> -1,
                        (index, chunk) -> {
                            Integer newIndex = index + 1;
                            if (log.isDebugEnabled()) {
                                log.debug("Sequence {}, Chunk size {}", newIndex, chunk.knownSize());
                            }
                            return Pair.create(newIndex, Pair.create(newIndex, chunk));
                        },
                        indexOnComplete -> Optional.empty()
                )
                .mapAsync(streamChunkParallelism, chunkPair -> {
                    Optional<Duration> requestTTL = cacheRequest.ttlSeconds().map(Duration::ofSeconds);
                    var imageblder = new ByteStringBuilder();
                    imageblder.addAll(chunkPair.second());
                    PayloadChunk payloadChunk = new PayloadChunk(chunkPair.first(), imageblder.result().toArray());
                    if (chunkPair.first() == 0) {
                        List<PayloadChunk> initChunks = new ArrayList<>(List.of(payloadChunk));
                        Cache initCache = new Cache(cacheRequest.cacheName(), cacheRequest.key(), requestTTL, false, objectSize, false, initChunks);
                        return componentClient.forEventSourcedEntity(cacheId)
                                .method(CacheEntity::set)
                                .invokeAsync(initCache)
                                .thenApply(result -> HttpResponses.created());
                    } else {
                        return componentClient.forEventSourcedEntity(cacheId)
                                .method(CacheEntity::setWithChunk)
                                .invokeAsync(payloadChunk)
                                .thenApply(result -> HttpResponses.created());
                    }
                })
                .runWith(Sink.ignore(), materializer);
    }

    private CompletionStage<Done> streamLargeObjectAsChunks(CacheRequest cacheRequest, int payloadSize, ByteString binaryPayload) {
        String cacheId = cacheRequest.cacheName().concat(cacheRequest.key());
        long chunks = payloadSize / maxPayloadSize;
        long remainder = payloadSize % maxPayloadSize;
        if (remainder > 0) chunks++;
        int chunkSize = (int) (payloadSize / chunks + 1); // just assume a fraction and bump

        if (log.isDebugEnabled()) {
            log.debug("streamLargeObjectAsChunks object size {} chunks {}, chunksize {}", payloadSize, chunks, chunkSize);
        }

        return Source.single(binaryPayload)
                .via(new Chunker(chunkSize))
                .statefulMap(
                        () -> -1,
                        (index, chunk) -> {
                            Integer newIndex = index + 1;
                            if (log.isDebugEnabled()) {
                                log.debug("Sequence {}, Chunk size {}", newIndex, chunk.knownSize());
                            }
                            return Pair.create(newIndex, Pair.create(newIndex, chunk));
                        },
                        indexOnComplete -> Optional.empty()
                )
                .mapAsync(streamChunkParallelism, chunkPair -> {
                    Optional<Duration> requestTTL = cacheRequest.ttlSeconds().map(Duration::ofSeconds);
                    var imageblder = new ByteStringBuilder();
                    imageblder.addAll(chunkPair.second());
                    PayloadChunk payloadChunk = new PayloadChunk(chunkPair.first(), imageblder.result().toArray());
                    if (chunkPair.first() == 0) {
                        List<PayloadChunk> initChunks = new ArrayList<>(List.of(payloadChunk));
                        Cache initCache = new Cache(cacheRequest.cacheName(), cacheRequest.key(), requestTTL, false, payloadSize, false, initChunks);
                        return componentClient.forEventSourcedEntity(cacheId)
                                .method(CacheEntity::set)
                                .invokeAsync(initCache)
                                .thenApply(result -> HttpResponses.created());
                    } else {
                        return componentClient.forEventSourcedEntity(cacheId)
                                .method(CacheEntity::setWithChunk)
                                .invokeAsync(payloadChunk)
                                .thenApply(result -> HttpResponses.created());
                    }
                })
                .runWith(Sink.ignore(), materializer);
    }


    /* this is the JSON version of set */
    public CompletionStage<HttpResponse> cache(CacheRequest cacheRequest) {
        return isCacheNameNeededFirst(cacheRequest.cacheName())
                .thenCompose(httpResponse -> {
                    if (httpResponse.status().isSuccess()) {
                        // based upon: "Entity command too large, request payload and metadata must not be more than 524201 bytes but was 1930695"
                        boolean largeObject = cacheRequest.value().length > maxPayloadSize;
                        if (log.isDebugEnabled() && largeObject) {
                            log.debug("max payload size is {}. large cache object detected. Size is {} bytes", maxPayloadSize, cacheRequest.value().length);
                        }
                        if (largeObject) {
                            CompletionStage<Done> streamResult = streamLargeObjectAsChunks(cacheRequest);
                            return streamResult
                                    .thenCompose(result -> {
                                        Optional<Duration> ttlSecs = cacheRequest.ttlSeconds().isPresent() ? Optional.of(Duration.ofSeconds(cacheRequest.ttlSeconds().get())) : Optional.empty();
                                        return scheduleTTLTimerIfNeeded(cacheRequest.cacheName(), cacheRequest.key(), ttlSecs)
                                                .thenApply(rs -> HttpResponses.created());
                                    });
                        } else {
                            return createCacheEntity(cacheRequest.cacheName(), cacheRequest.key(), createSmallCacheObject(cacheRequest));
                        }
                    }
                    return CompletableFuture.completedFuture(httpResponse);
                });
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    public CompletionStage<HttpResponse> cacheSet(Optional<String> org, String cacheName, String key, Integer ttlSeconds, HttpEntity.Strict strictRequestBody) {
        return isCacheNameNeededFirst(cacheName)
                .thenCompose(httpResponse -> {
                    if (httpResponse.status().isSuccess()) {
                        if (!strictRequestBody.getContentType().equals(BINARY_PAYLOAD))
                            throw HttpException.badRequest("This service only accepts " + BINARY_PAYLOAD);
                        else {
                            // based upon: "Entity command too large, request payload and metadata must not be more than 524201 bytes but was 1930695"
                            int payloadSize = strictRequestBody.getData().size();
                            boolean largeObject = payloadSize > maxPayloadSize;
                            if (log.isDebugEnabled() && largeObject) {
                                log.debug("max payload size is {}. large cache object detected. Size is {} bytes", maxPayloadSize, payloadSize);
                            }
                            Optional<Integer> ttlSecs = ttlSeconds > 0 ? Optional.of(ttlSeconds) : Optional.empty();
                            if (largeObject) {
                                CacheRequest cacheRequest = new CacheRequest(org, cacheName, key, ttlSecs);
                                CompletionStage<Done> streamResult = streamLargeObjectAsChunks(cacheRequest, payloadSize, strictRequestBody.getData());
                                return streamResult
                                        .thenCompose(result -> {
                                            Optional<Duration> ttl = ttlSecs.map(Duration::ofSeconds);
                                            return scheduleTTLTimerIfNeeded(cacheRequest.cacheName(), cacheRequest.key(), ttl)
                                                    .thenApply(rs -> HttpResponses.created());
                                        });
                            } else {
                                CacheRequest cacheRequest = new CacheRequest(org, cacheName, key, ttlSecs, strictRequestBody.getData().toArray());
                                return createCacheEntity(cacheRequest.cacheName(), cacheRequest.key(), createSmallCacheObject(cacheRequest));
                            }
                        }
                    }
                    return CompletableFuture.completedFuture(httpResponse);
                });
    }
    
    public CompletionStage<HttpResponse> cacheSet(Optional<String> org, String cacheName, String key, HttpEntity.Strict strictRequestBody) {
        return cacheSet(org, cacheName, key, 0, strictRequestBody);
    }

    private byte[] combineChunks(long totalBytes, List<PayloadChunk> chunks) {
        byte[] allByteArray = new byte[Long.valueOf(totalBytes).intValue()];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        for (PayloadChunk future : chunks) {
            buff.put(future.payload());
        }
        return buff.array();
    }

    private CompletionStage<CacheInternalGetResponse> getCache(String compoundKey) {
        return componentClient.forEventSourcedEntity(compoundKey)
                .method(CacheEntity::get)
                .invokeAsync();
    }

    // this is a JSON verison of GET
    public CompletionStage<CacheGetResponse> getCache(String cacheName, String key) {
        String compoundKey = cacheName.concat(key);
        return getCache(compoundKey)
                .thenCompose(internalGetResponse -> {
                    if (internalGetResponse.chunks() == 1) {
                        return CompletableFuture.completedFuture(new CacheGetResponse(internalGetResponse.cacheName(), internalGetResponse.key(), true, internalGetResponse.firstChunk().payload()));
                    }
                    else {
                        List<CompletableFuture<PayloadChunk>> getChunkFutures = new ArrayList<>();
                        // capture the initial chuck returned by the get
                        getChunkFutures.add(CompletableFuture.completedFuture(internalGetResponse.firstChunk()));
                        for (int i = 1; i < internalGetResponse.chunks(); i++) {
                            getChunkFutures.add(componentClient.forEventSourcedEntity(compoundKey)
                                    .method(CacheEntity::getChunk)
                                    .invokeAsync(i).toCompletableFuture()
                            );
                        }
                        return FutureHelper.allOf(getChunkFutures)
                                .thenApply(futures -> new CacheGetResponse(cacheName, key, true, combineChunks(internalGetResponse.totalBytes(), futures)))
                                .exceptionally(ex -> { // TODO: maybe do a retry w/ backoff, or do it with the client
                                    String msg = String.format("an exception occurred while retrieving chunks for cache %s key %s:", cacheName, key);
                                    log.error(msg, ex.getMessage());
                                    return new CacheGetResponse(cacheName, key, false, new byte[0]);
                                });
                    }
                })
                .exceptionally(ex -> new CacheGetResponse(cacheName, key, false, new byte[0]));
    }

    /*
     This is an alternate binary REST call that is now a default.
  
     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
  public CompletionStage<HttpResponse> getCacheGet(String cacheName, String key) {
    String compoundKey = cacheName.concat(key);
    return getCache(compoundKey)
            .thenCompose(internalGetResponse -> {
              if (internalGetResponse.chunks() == 1) {
                return CompletableFuture.completedFuture(HttpResponse.create().withEntity(internalGetResponse.firstChunk().payload()));
              }
              else {
                List<CompletableFuture<PayloadChunk>> getChunkFutures = new ArrayList<>();
                // capture the initial chuck returned by the get
                getChunkFutures.add(CompletableFuture.completedFuture(internalGetResponse.firstChunk()));
                for (int i = 1; i < internalGetResponse.chunks(); i++) {
                  getChunkFutures.add(componentClient.forEventSourcedEntity(compoundKey)
                          .method(CacheEntity::getChunk)
                          .invokeAsync(i).toCompletableFuture()
                  );
                }
                return FutureHelper.allOf(getChunkFutures)
                        .thenApply(futures -> HttpResponse.create().withEntity(combineChunks(internalGetResponse.totalBytes(), futures)))
                        .exceptionally(ex -> { // TODO: maybe do a retry w/ backoff, or do it with the client
                          String msg = String.format("an exception occurred while retrieving chunks for cache %s key %s:", cacheName, key);
                          log.error(msg, ex.getMessage());
                          return HttpResponses.internalServerError(msg);
                        });
              }
            })
            .exceptionally(ex -> HttpResponses.notFound());
  }
    
    public CompletionStage<CacheGetKeysResponse> getCacheKeys(String cacheName) {
        return componentClient.forView()
                .method(CacheView::getCacheKeys)
                .invokeAsync(cacheName)
                .thenApply(results -> new CacheGetKeysResponse(cacheName, results.keys()));
    }
    
    public CompletionStage<HttpResponse> delete(String cacheName, String key) {
        String compoundKey = cacheName.concat(key);
        return componentClient.forEventSourcedEntity(compoundKey)
                .method(CacheEntity::delete)
                .invokeAsync()
                .thenApply(__ -> {
                    // cancel a timer if active
                    timerScheduler.cancel(compoundKey);
                    return HttpResponses.accepted();
                });
    }

    public CompletionStage<BatchCacheResponse> cacheBatch(BatchCacheRequest batchCacheRequest) {
        List<CompletableFuture<BatchCacheRequestResults>> batchCacheRequestFutures = new ArrayList<>();
        batchCacheRequest.cacheRequests().forEach(cacheRequest -> batchCacheRequestFutures.add(
                cache(cacheRequest).thenApply(result -> new BatchCacheRequestResults(result, cacheRequest))
                        .toCompletableFuture()
        ));
        return FutureHelper.allOf(batchCacheRequestFutures)
                .thenApply(results -> new BatchCacheResponse(true, Collections.emptyList()))
                .exceptionally(ex -> {
                    // collect results from actual future list
                    List<BatchCacheResult> results = new ArrayList<>();
                    for (CompletableFuture<BatchCacheRequestResults> future : batchCacheRequestFutures) {
                        try {
                            CacheRequest cacheRequest = future.get().cacheRequest();
                            if (future.isCompletedExceptionally()) {
                                results.add(new BatchCacheResult(cacheRequest.cacheName(), cacheRequest.key(), false));
                            }
                            else {
                                results.add(new BatchCacheResult(cacheRequest.cacheName(), cacheRequest.key(), true));
                            }
                        }
                        catch (InterruptedException | ExecutionException e) {
                            log.error("cacheBatch: This shouldn't happen, but just in case: {}", ex.getMessage(), ex);
                        }
                    }
                    return new BatchCacheResponse(false, results);
                });
    }

    public CompletionStage<BatchGetCacheResponse> getCacheBatch(BatchGetCacheRequests getBatchRequests) {
        List<CompletableFuture<CacheGetResponse>> getBatchFutures = new ArrayList<>();
        for (BatchGetCacheRequest request : getBatchRequests.getCachedBatch()) {
            getBatchFutures.add(
                    getCache(request.cacheName(), request.key())
                            .toCompletableFuture()
            );
        }
        return FutureHelper.allOf(getBatchFutures)
                .thenApply(results -> new BatchGetCacheResponse(true, results))
                .exceptionally(ex -> {
                    // collect results from actual future list
                    List<CacheGetResponse> getCacheResults = new ArrayList<>();
                    getBatchFutures.forEach(goodOrBadResult -> {
                        try {
                            CacheGetResponse cacheGetResponse = goodOrBadResult.get();
                            getCacheResults.add(cacheGetResponse);
                        }
                        catch (InterruptedException | ExecutionException e) {
                            log.error("getCacheBatch: This shouldn't happen, but just in case: {}", ex.getMessage(), ex);
                        }
                    });
                    return new BatchGetCacheResponse(false, getCacheResults);
                });
    }

    public CompletionStage<BatchDeleteCacheResponse> deleteCacheBatch(BatchGetCacheRequests getBatchRequests) {
        List<CompletableFuture<CacheDeleteResponse>> getBatchFutures = new ArrayList<>();
        for (BatchGetCacheRequest request : getBatchRequests.getCachedBatch()) {
            getBatchFutures.add(
                    delete(request.cacheName(), request.key())
                            .thenApply(deleteResult -> {
                                Boolean success = deleteResult.status().isSuccess();
                                return new CacheDeleteResponse(request.cacheName(), request.key(), success);
                            })
                            .toCompletableFuture()
            );
        }
        return FutureHelper.allOf(getBatchFutures)
                .thenApply(results -> new BatchDeleteCacheResponse(true, results))
                .exceptionally(ex -> {
                    // collect results from actual future list
                    List<CacheDeleteResponse> getCacheResults = new ArrayList<>();
                    getBatchFutures.forEach(goodOrBadResult -> {
                        try {
                            CacheDeleteResponse cacheGetResponse = goodOrBadResult.get();
                            getCacheResults.add(cacheGetResponse);
                        }
                        catch (InterruptedException | ExecutionException e) {
                            log.error("deleteCacheBatch: This shouldn't happen, but just in case: {}", ex.getMessage(), ex);
                        }
                    });
                    return new BatchDeleteCacheResponse(false, getCacheResults);
                });
    }

    // Cache API -- END
}
