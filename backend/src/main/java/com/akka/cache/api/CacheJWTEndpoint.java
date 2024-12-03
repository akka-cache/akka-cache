package com.akka.cache.api;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint
@JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN, bearerTokenIssuers = "gcp")
public class CacheJWTEndpoint {
    private static final Logger log = LoggerFactory.getLogger(CacheJWTEndpoint.class);

    private static final String ORG = "org";
    private static final String SERVICE_LEVEL = "serviceLevel";
    
    private final CacheAPICoreImpl core;
    private final RequestContext requestContext;
    private final int rateLimitDefaultRPS;
    private final int rateLimitWaitTimeoutMillis;
    private final Map<String, String> claims;
    private final OrgClaims orgClaims;

    private record OrgStats(RateLimiter rateLimiter) {}

    private final Map<String, OrgStats> orgLimitMap = Maps.newConcurrentMap();

    public CacheJWTEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler, Materializer materializer, RequestContext requestContext) {
        this.requestContext = requestContext;
        this.core = new CacheAPICoreImpl(config, componentClient, timerScheduler, materializer);
        this.rateLimitDefaultRPS = config.getInt("app.rate-limit-default-request-per-second");
        this.rateLimitWaitTimeoutMillis = config.getInt("app.rate-limit-wait-timeout-millis");
        this.claims = requestContext.getJwtClaims().asMap();
        this.orgClaims = getOrgClaim();
    }

    private record OrgClaims(String org, String serviceLevel) {}

    private OrgClaims getOrgClaim() {
        String org = claims.get(ORG);
        if (org == null) {
            throw HttpException.badRequest(ORG + " not found in the JWT Token");
        }
        String serviceLevel = claims.get(SERVICE_LEVEL);
        if (serviceLevel == null) {
            throw HttpException.badRequest(SERVICE_LEVEL + " not found in the JWT Token");
        }
        return new OrgClaims(org, "");
    }

    private void doesExceedSubscription() {



    }

    // Cache Names -- BEGIN
    private CacheNameRequest modCacheNameWithOrg(CacheNameRequest request) {
        return new CacheNameRequest(orgClaims.org.concat(request.cacheName()), request.description());
    }

    @Post("/cacheName")
    public CompletionStage<HttpResponse> createCacheName(CacheNameRequest request) {
        doesExceedSubscription();
        return core.createCacheName(modCacheNameWithOrg(request));
    }

    @Put("/cacheName")
    public CompletionStage<HttpResponse> updateCacheName(CacheNameRequest request) {
        doesExceedSubscription();
        return core.updateCacheName(modCacheNameWithOrg(request));
    }

    @Get("/cacheName/{cacheName}")
    public CompletionStage<CacheName> getCacheName(String cacheName) {
        doesExceedSubscription();
        return core.getCacheName(orgClaims.org.concat(cacheName));
    }

    @Get("/cacheName/{cacheName}/keys")
    public CompletionStage<CacheView.CacheSummaries> getCacheKeyList(String cacheName) {
        doesExceedSubscription();
        return core.getCacheKeyList(orgClaims.org.concat(cacheName));
    }

    // This deletes the cacheName as well as all the keys
    @Delete("/cacheName/{cacheName}")
    public CompletionStage<HttpResponse> deleteCacheKeys(String cacheName) {
        doesExceedSubscription();
        return core.deleteCacheKeys(orgClaims.org.concat(cacheName));
    }

    // This deletes all the cached data but leaves the cacheName in place
    @Put("/cacheName/{cacheName}/flush")
    public CompletionStage<HttpResponse> flushCacheKeys(String cacheName) {
        doesExceedSubscription();
        return core.flushCacheKeys(orgClaims.org.concat(cacheName));
    }

    // Cache Names -- END

    // Cache API -- BEGIN

    /* this is the JSON version of set */
    private CacheRequest modCachNameCacheRequestWithOrg(CacheRequest cacheRequest) {
        return new CacheRequest(
                orgClaims.org.concat(cacheRequest.cacheName()),
                cacheRequest.key(),
                cacheRequest.ttlSeconds(),
                cacheRequest.value()
        );
    }

    @Post("/set")
    public CompletionStage<HttpResponse> cache(CacheRequest cacheRequest) {
        doesExceedSubscription();
        return core.cache(modCachNameCacheRequestWithOrg(cacheRequest));
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Post("/{cacheName}/{key}/{ttlSeconds}")
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, Integer ttlSeconds, HttpEntity.Strict strictRequestBody) {
        doesExceedSubscription();
        return core.cacheSet(orgClaims.org.concat(cacheName), key, ttlSeconds, strictRequestBody);
    }

    @Post("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, HttpEntity.Strict strictRequestBody) {
        doesExceedSubscription();        
        return core.cacheSet(orgClaims.org.concat(cacheName), key, 0, strictRequestBody);
    }

    // this is a JSON verison of GET
    @Get("/get/{cacheName}/{key}")
    public CompletionStage<CacheGetResponse> getCache(String cacheName, String key) {
        doesExceedSubscription();
        return core.getCache(orgClaims.org.concat(cacheName), key);
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Get("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> getCacheGet(String cacheName, String key) {
        doesExceedSubscription();
        return core.getCacheGet(orgClaims.org.concat(cacheName), key);
    }

    @Get("/{cacheName}/keys")
    public CompletionStage<CacheGetKeysResponse> getCacheKeys(String cacheName) {
        doesExceedSubscription();
        return core.getCacheKeys(orgClaims.org.concat(cacheName));
    }

    @Delete("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> delete(String cacheName, String key) {
        doesExceedSubscription();
        return core.delete(orgClaims.org.concat(cacheName), key);
    }

    BatchCacheRequest addOrgToBatchRequest(BatchCacheRequest batchCacheRequest) {
        List<CacheRequest> cacheRequests = new ArrayList<>();
        batchCacheRequest.cacheRequests().forEach(cacheRequest -> {
            cacheRequests.add(modCachNameCacheRequestWithOrg(cacheRequest));
        });
        return new BatchCacheRequest(cacheRequests);
    }

    @Post("/batch/")
    public CompletionStage<BatchCacheResponse> cacheBatch(BatchCacheRequest batchCacheRequest) {
        doesExceedSubscription();
        return core.cacheBatch(addOrgToBatchRequest(batchCacheRequest));
    }

    BatchGetCacheRequests addOrgToBatchGetRequest(BatchGetCacheRequests batchCacheRequest) {
        List<BatchGetCacheRequest> batchGetCacheRequests = new ArrayList<>();
        batchCacheRequest.getCachedBatch().forEach(getCacheBatch -> {
            batchGetCacheRequests.add(new BatchGetCacheRequest(orgClaims.org.concat(getCacheBatch.cacheName()), getCacheBatch.key()));
        });
        return new BatchGetCacheRequests(batchGetCacheRequests);
    }

    @Post("/batch/get")
    public CompletionStage<BatchGetCacheResponse> getCacheBatch(BatchGetCacheRequests batchGetCacheRequests) {
        doesExceedSubscription();
        return core.getCacheBatch(addOrgToBatchGetRequest(batchGetCacheRequests));
    }

    @Delete("/batch/")
    public CompletionStage<BatchDeleteCacheResponse> deleteCacheBatch(BatchGetCacheRequests batchGetCacheRequests) {
        doesExceedSubscription();
        return core.deleteCacheBatch(addOrgToBatchGetRequest(batchGetCacheRequests));
    }

    // Cache API -- END
}
