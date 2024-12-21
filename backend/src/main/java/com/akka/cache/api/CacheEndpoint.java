package com.akka.cache.api;

import akka.http.javadsl.model.*;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.Materializer;
import com.akka.cache.application.CacheView;
import com.akka.cache.domain.*;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.akka.cache.domain.CacheAPI.*;

import static com.akka.cache.api.EndpointConstants.*;

@Acl(allow = @Acl.Matcher(service = "*"))
@HttpEndpoint("/cache")
public class CacheEndpoint {

    // TODO: delete if not needed
    private static final Logger log = LoggerFactory.getLogger(CacheEndpoint.class);

    private final CacheAPICoreImpl core;

    /**
     * @param config the configuration for the component
     * @param componentClient the client which is the entry point for the component
     * @param timerScheduler the timer scheduler for the component
     * @param materializer the materializer for the component
     *
     * This is the constructor for the CacheEndpoint. It is used by the Akka framework to create an instance of this class.
     */
    public CacheEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler, Materializer materializer) {
        core = new CacheAPICoreImpl(config, componentClient, timerScheduler, materializer);
    }

    // Cache Names -- BEGIN

    /**
     * This endpoint is used to create a new cache name.
     *
     * @param request the request containing the cache name and any additional metadata
     * @return a response containing the newly created cache name, of type {@link CacheName}.
     */
    @Post("/cacheName")
    public CompletionStage<HttpResponse> createCacheName(CacheNameRequest request) {
        return core.createCacheName(request);
    }

    @Put("/cacheName")
    public CompletionStage<HttpResponse> updateCacheName(CacheNameRequest request) {
        return core.updateCacheName(request);
    }

    @Get("/cacheName/{cacheName}")
    public CompletionStage<CacheName> getCacheName(String cacheName) {
        return core.getCacheName(cacheName);
    }

    @Get("/cacheName/{cacheName}/keys")
    public CompletionStage<CacheView.CacheSummaries> getCacheKeyList(String cacheName) {
        return core.getCacheKeyList(cacheName);
    }

    // This deletes the cacheName as well as all the keys
    @Delete("/cacheName/{cacheName}")
    public CompletionStage<HttpResponse> deleteCacheKeys(String cacheName) {
        return core.deleteCacheKeys(cacheName, false);
    }

    // This deletes all the cached data but leaves the cacheName in place
    @Put("/cacheName/{cacheName}/flush")
    public CompletionStage<HttpResponse> flushCacheKeys(String cacheName) {
        return core.deleteCacheKeys(cacheName, true);
    }

    // Cache Names -- END

    // Cache API -- BEGIN

    /* this is the JSON version of set */
    @Post("/set")
    public CompletionStage<HttpResponse> cache(CacheRequest cacheRequest) {
        return core.cache(cacheRequest);
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Post("/{cacheName}/{key}/{ttlSeconds}")
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, Integer ttlSeconds, HttpEntity.Strict strictRequestBody) {
        return core.cacheSet(Optional.empty(), cacheName, key, ttlSeconds, strictRequestBody);
    }

    @Post("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, HttpEntity.Strict strictRequestBody) {
        return core.cacheSet(Optional.empty(), cacheName, key, DEFAULT_TTL, strictRequestBody);
    }

    // this is a JSON verison of GET
    @Get("/get/{cacheName}/{key}")
    public CompletionStage<CacheGetResponse> getCache(String cacheName, String key) {
        return core.getCache(cacheName, key);
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Get("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> getCacheGet(String cacheName, String key) {
        return core.getCacheGet(cacheName, key);
    }

    @Get("/{cacheName}/keys")
    public CompletionStage<CacheGetKeysResponse> getCacheKeys(String cacheName) {
        return core.getCacheKeys(cacheName);
    }

    @Delete("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> delete(String cacheName, String key) {
        return core.delete(cacheName, key);
    }

    @Post("/batch")
    public CompletionStage<BatchCacheResponse> cacheBatch(BatchCacheRequest batchCacheRequest) {
        return core.cacheBatch(batchCacheRequest);
    }

    @Post("/batch/get")
    public CompletionStage<BatchGetCacheResponse> getCacheBatch(BatchGetCacheRequests getBatchRequests) {
        return core.getCacheBatch(getBatchRequests);
    }

    @Delete("/batch")
    public CompletionStage<BatchDeleteCacheResponse> deleteCacheBatch(BatchGetCacheRequests getBatchRequests) {
        return core.deleteCacheBatch(getBatchRequests);
    }

    // Cache API -- END
}
