package com.akka.cache.api;

import akka.Done;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.RequestContext;
import akka.javasdk.timer.TimerScheduler;
import akka.javasdk.annotations.JWT;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.stream.Materializer;
import com.akka.cache.application.CacheView;
import com.akka.cache.domain.CacheAPI.*;
import com.akka.cache.domain.CacheName;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint
@JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN, bearerTokenIssuers = "gcp?")
public class CacheJWTEndpoint {
    private static final Logger log = LoggerFactory.getLogger(CacheJWTEndpoint.class);

    private final CacheAPICoreImpl core;
    private final RequestContext requestContext;
    private final int rateLimitDefaultRPS;
    private final int rateLimitWaitTimeoutMillis;

    private record OrgStats(RateLimiter rateLimiter) {}

    private final Map<String, OrgStats> orgLimitMap = Maps.newConcurrentMap();

    public CacheJWTEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler, Materializer materializer, RequestContext requestContext) {
        this.requestContext = requestContext;
        this.core = new CacheAPICoreImpl(config, componentClient, timerScheduler, materializer);
        this.rateLimitDefaultRPS = config.getInt("app.rate-limit-default-request-per-second");
        this.rateLimitWaitTimeoutMillis = config.getInt("app.rate-limit-wait-timeout-millis");
    }

    private record OrgClaims(String org, String serviceLevel) {}

    private OrgClaims getOrgClaim() {
        Map<String, String> claims = requestContext.getJwtClaims().asMap();
        String org = claims.get("org");
        if (org == null) {
            throw HttpException.badRequest("org not found in the JWT Token");
        }
        // TODO: pick up the service level
        return new OrgClaims(org, "");
    }

    private void maybeThrottle(int permits) {
        OrgClaims orgClaims = getOrgClaim();
        RateLimiter rateLimiter;
        // Verify whether the cache has a hit key.
        if (!orgLimitMap.containsKey(orgClaims.org)) {
            // Create a token bucket.
            // TODO: account for service level
            rateLimiter = RateLimiter.create(rateLimitDefaultRPS);
            orgLimitMap.put(orgClaims.org, new OrgStats(rateLimiter));
            if (log.isDebugEnabled()) {
                log.debug("New rate limit token bucket={},Capacity={}", orgClaims.org, rateLimitDefaultRPS);
            }
        }
        rateLimiter = orgLimitMap.get(orgClaims.org).rateLimiter;
        // Acquire the token.

        // ths potentially blocks for rateLimitWaitTimeoutMillis if permits aren't available
        boolean acquire = rateLimiter.tryAcquire(permits, rateLimitWaitTimeoutMillis, TimeUnit.MILLISECONDS);
        // Fail to acquire the permits. The exception message returned.
        // TODO: should we do our own non-blocking throttle, with future?
        if (!acquire) {
            String msg = String.format("Token bucket=%s, Token acquisition failed", orgClaims.org);
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }
            throw HttpException.badRequest(msg);
        }
    }

    // Cache Names -- BEGIN

    @Post("/cacheName")
    public CompletionStage<HttpResponse> createCacheName(CacheNameRequest request) {
        maybeThrottle(1);
        return core.createCacheName(request);
    }

    @Put("/cacheName")
    public CompletionStage<HttpResponse> updateCacheName(CacheNameRequest request) {
        maybeThrottle(1);
        return core.updateCacheName(request);
    }

    @Get("/cacheName/{cacheName}")
    public CompletionStage<CacheName> getCacheName(String cacheName) {
        maybeThrottle(1);
        return core.getCacheName(cacheName);
    }

    @Get("/cacheName/{cacheName}/keys")
    public CompletionStage<CacheView.CacheSummaries> getCacheKeyList(String cacheName) {
        maybeThrottle(1);
        return core.getCacheKeyList(cacheName);
    }

    // This deletes the cacheName as well as all the keys
    @Delete("/cacheName/{cacheName}")
    public CompletionStage<HttpResponse> deleteCacheKeys(String cacheName) {
        maybeThrottle(1);
        return core.deleteCacheKeys(cacheName);
    }

    // This deletes all the cached data but leaves the cacheName in place
    @Put("/cacheName/{cacheName}/flush")
    public CompletionStage<HttpResponse> flushCacheKeys(String cacheName) {
        maybeThrottle(1);
        return core.flushCacheKeys(cacheName);
    }

    // Cache Names -- END

    // Cache API -- BEGIN

    /* this is the JSON version of set */
    @Post("/set")
    public CompletionStage<HttpResponse> cache(CacheRequest cacheRequest) {
        maybeThrottle(1);
        return core.cache(cacheRequest);
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Post("/{cacheName}/{key}/{ttlSeconds}")
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, Integer ttlSeconds, HttpEntity.Strict strictRequestBody) {
        maybeThrottle(1);
        return core.cacheSet(cacheName, key, ttlSeconds, strictRequestBody);
    }

    @Post("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, HttpEntity.Strict strictRequestBody) {
        maybeThrottle(1);        
        return core.cacheSet(cacheName, key, 0, strictRequestBody);
    }

    // this is a JSON verison of GET
    @Get("/get/{cacheName}/{key}")
    public CompletionStage<CacheGetResponse> getCache(String cacheName, String key) {
        maybeThrottle(1);
        return core.getCache(cacheName, key);
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Get("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> getCacheGet(String cacheName, String key) {
        maybeThrottle(1);
        return core.getCacheGet(cacheName, key);
    }

    @Get("/{cacheName}/keys")
    public CompletionStage<CacheGetKeysResponse> getCacheKeys(String cacheName) {
        maybeThrottle(1);
        return core.getCacheKeys(cacheName);
    }

    @Delete("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> delete(String cacheName, String key) {
        maybeThrottle(1);
        return core.delete(cacheName, key);
    }

    @Post("/batch/")
    public CompletionStage<BatchCacheResponse> cacheBatch(BatchCacheRequest batchCacheRequest) {
        maybeThrottle(1);
        return core.cacheBatch(batchCacheRequest);
    }

    @Post("/batch/get")
    public CompletionStage<BatchGetCacheResponse> getCacheBatch(BatchGetCacheRequests getBatchRequests) {
        maybeThrottle(1);
        return core.getCacheBatch(getBatchRequests);
    }

    @Delete("/batch/")
    public CompletionStage<BatchDeleteCacheResponse> deleteCacheBatch(BatchGetCacheRequests getBatchRequests) {
        maybeThrottle(1);
        return core.deleteCacheBatch(getBatchRequests);
    }

    // Cache API -- END
}
