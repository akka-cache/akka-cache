package com.akka.cache.api;

import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.Materializer;
import com.akka.cache.application.CacheView;
import com.akka.cache.domain.CacheAPI.*;
import com.akka.cache.domain.CacheName;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint
public class CacheEndpointExternal {
    private static final Logger log = LoggerFactory.getLogger(CacheEndpointExternal.class);

    private final CacheAPICoreImpl core;

    public CacheEndpointExternal(Config config, ComponentClient componentClient, TimerScheduler timerScheduler, Materializer materializer) {
        core = new CacheAPICoreImpl(config, componentClient, timerScheduler, materializer);
    }

    // Cache Names -- BEGIN

    @Post("/cacheName")
    public CompletionStage<HttpResponse> create(CacheNameRequest request) {
        return core.create(request);
    }

    @Put("/cacheName")
    public CompletionStage<HttpResponse> update(CacheNameRequest request) {
        return core.update(request);
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
        return core.deleteCacheKeys(cacheName);
    }

    // This deletes all the cached data but leaves the cacheName in place
    @Put("/cacheName/{cacheName}/flush")
    public CompletionStage<HttpResponse> flushCacheKeys(String cacheName) {
        return core.flushCacheKeys(cacheName);
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
        return core.cacheSet(cacheName, key, ttlSeconds, strictRequestBody);
    }

    @Post("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, HttpEntity.Strict strictRequestBody) {
        return core.cacheSet(cacheName, key, 0, strictRequestBody);
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

    @Post("/batch/")
    public CompletionStage<BatchCacheResponse> cacheBatch(BatchCacheRequest batchCacheRequest) {
        return core.cacheBatch(batchCacheRequest);
    }

    @Post("/batch/get")
    public CompletionStage<BatchGetCacheResponse> getCacheBatch(BatchGetCacheRequests getBatchRequests) {
        return core.getCacheBatch(getBatchRequests);
    }

    @Delete("/batch/")
    public CompletionStage<BatchDeleteCacheResponse> deleteCacheBatch(BatchGetCacheRequests getBatchRequests) {
        return core.deleteCacheBatch(getBatchRequests);
    }

    // Cache API -- END
}