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

//Use this ACL if you want to experiment in unsecure mode
//@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint(CACHE_PATH)
public class CacheEndpoint {
    // TODO: delete if not needed
    private static final Logger log = LoggerFactory.getLogger(CacheEndpoint.class);

    private final CacheAPICoreImpl core;

    public CacheEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler, Materializer materializer) {
        core = new CacheAPICoreImpl(config, componentClient, timerScheduler, materializer);
    }

    // Cache Names -- BEGIN

    @Post(CACHE_NAME_PATH)
    public CompletionStage<HttpResponse> createCacheName(CacheNameRequest request) {
        return core.createCacheName(request);
    }

    @Put(CACHE_NAME_PATH)
    public CompletionStage<HttpResponse> updateCacheName(CacheNameRequest request) {
        return core.updateCacheName(request);
    }

    @Get(CACHE_NAME_PATH + CACHE_NAME_REPLACE_PATH)
    public CompletionStage<CacheName> getCacheName(String cacheName) {
        return core.getCacheName(cacheName);
    }

    @Get(CACHE_NAME_PATH + CACHE_NAME_REPLACE_PATH + KEYS_PATH)
    public CompletionStage<CacheView.CacheSummaries> getCacheKeyList(String cacheName) {
        return core.getCacheKeyList(cacheName);
    }

    // This deletes the cacheName as well as all the keys
    @Delete(CACHE_NAME_PATH + CACHE_NAME_REPLACE_PATH)
    public CompletionStage<HttpResponse> deleteCacheKeys(String cacheName) {
        return core.deleteCacheKeys(cacheName, false);
    }

    // This deletes all the cached data but leaves the cacheName in place
    @Put(CACHE_NAME_PATH + CACHE_NAME_REPLACE_PATH + FLUSH_PATH)
    public CompletionStage<HttpResponse> flushCacheKeys(String cacheName) {
        return core.deleteCacheKeys(cacheName, true);
    }

    // Cache Names -- END

    // Cache API -- BEGIN

    /* this is the JSON version of set */
    @Post(SET_PATH)
    public CompletionStage<HttpResponse> cache(CacheRequest cacheRequest) {
        return core.cache(cacheRequest);
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Post(CACHE_NAME_REPLACE_PATH + KEY_REPLACE_PATH + TTL_REPLACE_PATH)
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, Integer ttlSeconds, HttpEntity.Strict strictRequestBody) {
        return core.cacheSet(Optional.empty(), cacheName, key, ttlSeconds, strictRequestBody);
    }

    @Post(CACHE_NAME_REPLACE_PATH + KEY_REPLACE_PATH)
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, HttpEntity.Strict strictRequestBody) {
        return core.cacheSet(Optional.empty(), cacheName, key, DEFAULT_TTL, strictRequestBody);
    }

    // this is a JSON verison of GET
    @Get(GET_PATH + CACHE_NAME_REPLACE_PATH + KEY_REPLACE_PATH)
    public CompletionStage<CacheGetResponse> getCache(String cacheName, String key) {
        return core.getCache(cacheName, key);
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Get(CACHE_NAME_REPLACE_PATH + KEY_REPLACE_PATH)
    public CompletionStage<HttpResponse> getCacheGet(String cacheName, String key) {
        return core.getCacheGet(cacheName, key);
    }

    @Get(CACHE_NAME_REPLACE_PATH + KEYS_PATH)
    public CompletionStage<CacheGetKeysResponse> getCacheKeys(String cacheName) {
        return core.getCacheKeys(cacheName);
    }

    @Delete(CACHE_NAME_REPLACE_PATH + KEY_REPLACE_PATH)
    public CompletionStage<HttpResponse> delete(String cacheName, String key) {
        return core.delete(cacheName, key);
    }

    @Post(BATCH_PATH)
    public CompletionStage<BatchCacheResponse> cacheBatch(BatchCacheRequest batchCacheRequest) {
        return core.cacheBatch(batchCacheRequest);
    }

    @Post(BATCH_PATH + GET_PATH)
    public CompletionStage<BatchGetCacheResponse> getCacheBatch(BatchGetCacheRequests getBatchRequests) {
        return core.getCacheBatch(getBatchRequests);
    }

    @Delete(BATCH_PATH)
    public CompletionStage<BatchDeleteCacheResponse> deleteCacheBatch(BatchGetCacheRequests getBatchRequests) {
        return core.deleteCacheBatch(getBatchRequests);
    }

    // Cache API -- END
}
