package com.akka.cache.api;

import akka.Done;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Pair;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import akka.util.ByteStringBuilder;
import akka.stream.Materializer;
import com.akka.cache.application.CacheNameEntity;
import com.akka.cache.application.CacheEntity;
import com.akka.cache.application.CacheView;
import com.akka.cache.application.CacheTimedAction;
import com.akka.cache.application.CacheNameDeleteWorkflow;
import com.akka.cache.domain.*;
import com.akka.cache.streams.Chunker;
import com.akka.cache.utils.FutureHelper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/cache")
public class CacheEndpoint {
  private static final Logger log = LoggerFactory.getLogger(CacheEndpoint.class);

  private final ComponentClient componentClient;
  private final boolean cacheNameNeededFirst;
  private final TimerScheduler timerScheduler;
  private final Optional<Duration> defaultTTL;
  private final Materializer materializer;
  private final int streamChunkParallelism;
  private final long maxPayloadSize;

  public CacheEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler, Materializer materializer) {
    this.componentClient = componentClient;
    this.timerScheduler = timerScheduler;
    this.materializer = materializer;
    this.streamChunkParallelism = config.getInt("app.stream-chunk-parallelism");
    this.maxPayloadSize = config.getLong("app.stream-max-payload-size");
    cacheNameNeededFirst = config.getBoolean("app.cache-name-needed-first");
    if (config.hasPath("app.default-default-ttl")) {
      defaultTTL = Optional.of(config.getDuration("app.default-default-ttl"));
    }
    else {
      defaultTTL = Optional.empty();
    }
  }


  /*
  Note: The only real reason to have a separate cacheName
    entity is to maintain a description of the cache, and potentially additional settings
   */
  // Cache Names -- BEGIN
  public record CacheNameRequest(String cacheName, String description) {}

  @Post("/cacheName")
  public CompletionStage<HttpResponse> create(CacheNameRequest request) {
    CacheName cn = new CacheName(request.cacheName, Optional.of(request.description));
    return componentClient.forEventSourcedEntity(cn.cacheName())
            .method(CacheNameEntity::create)
            .invokeAsync(cn)
            .thenApply(__ -> HttpResponses.created());
  }

  @Put("/cacheName")
  public CompletionStage<HttpResponse> update(CacheNameRequest request) {
    CacheName cn = new CacheName(request.cacheName, Optional.of(request.description));
    return componentClient.forEventSourcedEntity(cn.cacheName())
            .method(CacheNameEntity::update)
            .invokeAsync(cn)
            .thenApply(__ -> HttpResponses.accepted());
  }

  @Get("/cacheName/{cacheName}")
  public CompletionStage<CacheName> getCacheName(String cacheName) {
    return componentClient.forEventSourcedEntity(cacheName)
            .method(CacheNameEntity::get)
            .invokeAsync()
            .thenApply(c -> c);
  }

  @Get("/cacheName/{cacheName}/keys")
  public CompletionStage<CacheView.CacheSummaries> getCacheKeyList(String cacheName) {
    return componentClient.forView()
            .method(CacheView::getCacheSummaries)
            .invokeAsync(cacheName)
            .exceptionally(ex -> {
              throw HttpException.badRequest("No keys items found for ".concat(cacheName));
            });
  }

  // This deletes the cachName as well as all the keys
  @Delete("/cacheName/{cacheName}")
  public CompletionStage<HttpResponse> deleteCacheKeys(String cacheName) {
    var startDeletionSetup = new CacheNameDeleteWorkflow.StartDeletionsSetup(cacheName, false);
    return componentClient.forWorkflow(cacheName)
            .method(CacheNameDeleteWorkflow::startDeletions)
            .invokeAsync(startDeletionSetup)
            .thenApply(transferState -> HttpResponses.accepted());
  }

  // This deletes all the cached data but leaves the cacheName in place
  @Put("/cacheName/{cacheName}/flush")
  public CompletionStage<HttpResponse> flushCacheKeys(String cacheName) {
    var startDeletionSetup = new CacheNameDeleteWorkflow.StartDeletionsSetup(cacheName, true);
    return componentClient.forWorkflow(cacheName)
            .method(CacheNameDeleteWorkflow::startDeletions)
            .invokeAsync(startDeletionSetup)
            .thenApply(transferState -> HttpResponses.accepted());
  }


  // Cache Names -- END

  // Cache API -- BEGIN

  public record CacheRequest(String cacheName, String key, Optional<Integer> ttlSeconds, byte[] value) {
    public CacheRequest(String cacheName, String key, byte[] value) {
      this(cacheName, key, Optional.empty(), value);
    }
  }

  private CompletionStage<Done> scheduleTTLTimerIfNeeded(String cacheName, String key, Optional<Duration> requestTTLSeconds) {
    Optional<Duration> ttlSeconds = Optional.empty();
    String cacheId = cacheName.concat(key);
    if (requestTTLSeconds.isPresent()) {
      ttlSeconds = requestTTLSeconds;
    } else if (defaultTTL.isPresent()) {
      ttlSeconds = defaultTTL;
    }
    if (ttlSeconds.isPresent()) {
      if (log.isDebugEnabled()) {
        log.debug("starting TTL timer for {} {} {}", cacheName, key, ttlSeconds.get().getSeconds());
      }
      return timerScheduler.startSingleTimer(
              "keys".concat(cacheId),
              ttlSeconds.get(),
              componentClient.forTimedAction()
                      .method(CacheTimedAction::expireCacheTTL)
                      .deferred(cacheId)
      );
    } else {
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
    Optional<Duration> requestTTL = cacheRequest.ttlSeconds.map(Duration::ofSeconds);
    List<PayloadChunk> chunks = new ArrayList<>(List.of(new PayloadChunk(0, cacheRequest.value)));
    return new Cache(cacheRequest.cacheName, cacheRequest.key, requestTTL, false, false, chunks);
  }

  private CompletionStage<HttpResponse> isCacheNameNeededFirst(String cacheName) {
    if (cacheNameNeededFirst) {
      CompletionStage<CacheName> cName = getCacheName(cacheName);
      return cName.thenCompose(c -> {
        if (c.cacheName().isEmpty()) {
          return CompletableFuture.completedFuture(HttpResponses.notFound());
        } else {
          return CompletableFuture.completedFuture(HttpResponses.accepted());
        }
      });
    }
    else {
      return CompletableFuture.completedFuture(HttpResponses.accepted());
    }
  }

  // large object so we need to chunk it up to the entity
  private CompletionStage<Done> streamLargeObjectAsChunks(CacheRequest cacheRequest) {
    String cacheId = cacheRequest.cacheName.concat(cacheRequest.key);
    int objectSize = cacheRequest.value.length;
    long chunks = objectSize / maxPayloadSize;
    long remainder = objectSize % maxPayloadSize;
    if (remainder > 0) chunks++;
    int chunkSize = (int) (objectSize / chunks + 1); // just assume a fraction and bump

    if (log.isDebugEnabled()) {
      log.debug("streamLargeObjectAsChunks object size {} chunks {}, chunksize {}", objectSize, chunks, chunkSize);
    }

    ByteString valueBytes = new ByteStringBuilder().putBytes(cacheRequest.value).result();
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
              Optional<Duration> requestTTL = cacheRequest.ttlSeconds.map(Duration::ofSeconds);
              var imageblder = new ByteStringBuilder();
              imageblder.addAll(chunkPair.second());
              PayloadChunk payloadChunk = new PayloadChunk(chunkPair.first(), imageblder.result().toArray());
              if (chunkPair.first() == 0) {
                List<PayloadChunk> initChunks = new ArrayList<>(List.of(payloadChunk));
                Cache initCache = new Cache(cacheRequest.cacheName, cacheRequest.key, requestTTL, false, false, initChunks);
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

  @Post("/")
  public CompletionStage<HttpResponse> cache(CacheRequest cacheRequest) {
    return isCacheNameNeededFirst(cacheRequest.cacheName)
            .thenCompose(result -> {
              if (result.status().isSuccess()) {
                // based upon: "Entity command too large, request payload and metadata must not be more than 524201 bytes but was 1930695"
                boolean largeObject = cacheRequest.value.length > maxPayloadSize;
                if (log.isDebugEnabled() && largeObject) {
                  log.debug("max payload size is {}. large cache object detected. Size is {} bytes", maxPayloadSize, cacheRequest.value.length);
                }
                if (largeObject) {
                  CompletionStage<Done> streamResult = streamLargeObjectAsChunks(cacheRequest);
                  return streamResult.thenApply(rs -> HttpResponses.created());
                } else {
                  return createCacheEntity(cacheRequest.cacheName, cacheRequest.key, createSmallCacheObject(cacheRequest));
                }
              }
              return CompletableFuture.completedFuture(result);
            });
  }

  private CompletionStage<CacheInternalGetResponse> getCache(String compoundKey) {
    return componentClient.forEventSourcedEntity(compoundKey)
            .method(CacheEntity::get)
            .invokeAsync();
  }

  public record CacheGetResponse(String cacheName, String key, Boolean success, byte[] value) {}

  @Get("/{cacheName}/{key}")
  public CompletionStage<CacheGetResponse> getCache(String cacheName, String key) {
    String compoundKey = cacheName.concat(key);
    return getCache(compoundKey)
            .thenCompose(internalGetResponse -> {
              if (internalGetResponse.chunks() == 1) {
                return CompletableFuture.completedFuture(new CacheGetResponse(internalGetResponse.cacheName(), internalGetResponse.key(), true, internalGetResponse.firstChunk().payload()));
              } else {
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
                        .thenApply(futures -> {
                          var completeObject = new ByteStringBuilder();
                          futures.forEach(byteChunk -> {
                            // convert each payload into a ByteString
                            ByteString bytes = new ByteStringBuilder().putBytes(byteChunk.payload()).result();
                            // concat each ByteString into the total object
                            completeObject.addAll(bytes);
                          });
                          // convert the total object back into byte[]
                          return new CacheGetResponse(cacheName, key, true, completeObject.result().toArray());
                        })
                        .exceptionally(ex -> { // TODO: retry w/ backoff
                          String msg = String.format("an exception occurred while retrieving chunks for cache %s key %s:", cacheName, key);
                          log.error(msg, ex.getMessage());
                          return new CacheGetResponse(cacheName, key, false, new byte[0]);
                        });
              }
            });
  }

  public record CacheGetKeysResponse(String cacheName, List<String> keys) {}

  @Get("/{cacheName}/keys")
  public CompletionStage<CacheGetKeysResponse> getCacheKeys(String cacheName) {
    return componentClient.forView()
            .method(CacheView::getCacheKeys)
            .invokeAsync(cacheName)
            .thenApply(results -> new CacheGetKeysResponse(cacheName, results.keys()));
  }

  @Delete("/{cacheName}/{key}")
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

  public record BatchCacheRequest(List<CacheRequest> cacheRequests) {}

  public record BatchCacheRequestResults(HttpResponse httpResponse, CacheRequest cacheRequest) {}

  public record BatchCacheResult(String cacheName, String key, Boolean success) {}

  public record BatchCacheResponse(Boolean complete, List<BatchCacheResult> results) {}

  @Post("/batch/")
  public CompletionStage<BatchCacheResponse> cacheBatch(BatchCacheRequest batchCacheRequest) {
    List<CompletableFuture<BatchCacheRequestResults>> batchCacheRequestFutures = new ArrayList<>();
    batchCacheRequest.cacheRequests.forEach(cacheRequest -> batchCacheRequestFutures.add(
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
                  CacheRequest cacheRequest = future.get().cacheRequest;
                  if (future.isCompletedExceptionally()) {
                    results.add(new BatchCacheResult(cacheRequest.cacheName, cacheRequest.key, false));
                  }
                  else {
                    results.add(new BatchCacheResult(cacheRequest.cacheName, cacheRequest.key, true));
                  }
                }
                catch (InterruptedException | ExecutionException e) {
                  log.error("cacheBatch: This shouldn't happen, but just in case: {}", ex.getMessage(), ex);
                }
              }
              return new BatchCacheResponse(false, results);
            });
  }

  public record BatchGetCacheRequest(String cacheName, String key) {}
  public record BatchGetCacheRequests(List<BatchGetCacheRequest> getCachedBatch) {}
  public record BatchGetCacheResponse(Boolean complete, List<CacheGetResponse> results) {}

  @Post("/batch/get")
  public CompletionStage<BatchGetCacheResponse> getCacheBatch(BatchGetCacheRequests getBatchRequests) {
    List<CompletableFuture<CacheGetResponse>> getBatchFutures = new ArrayList<>();
      for (BatchGetCacheRequest request : getBatchRequests.getCachedBatch) {
          getBatchFutures.add(
                  getCache(request.cacheName, request.key)
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

  public record CacheDeleteResponse(String cacheName, String key, Boolean success) {}

  public record BatchDeleteCacheResponse(Boolean success, List<CacheDeleteResponse> cacheDeleteResponses) {}

  @Delete("/batch/")
  public CompletionStage<BatchDeleteCacheResponse> deleteCacheBatch(BatchGetCacheRequests getBatchRequests) {
    List<CompletableFuture<CacheDeleteResponse>> getBatchFutures = new ArrayList<>();
    for (BatchGetCacheRequest request : getBatchRequests.getCachedBatch) {
      getBatchFutures.add(
              delete(request.cacheName, request.key)
                      .thenApply(deleteResult -> {
                        Boolean success = deleteResult.status().isSuccess() ? true : false;
                        return new CacheDeleteResponse(request.cacheName(), request.key, success);
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
