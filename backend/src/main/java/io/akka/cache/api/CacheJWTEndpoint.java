package io.akka.cache.api;

import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.JwtClaims;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import akka.javasdk.http.RequestContext;
import akka.javasdk.timer.TimerScheduler;
import akka.javasdk.annotations.JWT;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.stream.Materializer;
import io.akka.cache.application.CacheView;
import io.akka.cache.application.OrgEntity;
import io.akka.cache.domain.CacheAPI.*;
import io.akka.cache.domain.CacheName;
import io.akka.cache.domain.Organization;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static io.akka.cache.api.EndpointConstants.*;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint
@JWT(validate = JWT.JwtMethodMode.BEARER_TOKEN, bearerTokenIssuers = "https://session.firebase.google.com/akka-cache")
public class CacheJWTEndpoint {
    private static final Logger log = LoggerFactory.getLogger(CacheJWTEndpoint.class);

    private final ComponentClient componentClient;
    
    private final CacheAPICoreImpl core;
    
    // TODO: delete if not needed
    private final RequestContext requestContext;

    private boolean enableOrgServiceLevelSaas;
    private long freeServiceMaxCachedBytes;

    private final JwtClaims claims;
    private final OrgClaims orgClaims;

    public CacheJWTEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler, Materializer materializer, RequestContext requestContext) {
        this.requestContext = requestContext;
        this.componentClient = componentClient;

        this.core = new CacheAPICoreImpl(config, componentClient, timerScheduler, materializer);

        this.enableOrgServiceLevelSaas = config.getBoolean("app.enable-org-service-level-saas");
        this.freeServiceMaxCachedBytes = config.getLong("app.free-service-level-max-bytes");

        var xxx = requestContext.getJwtClaims();
        this.claims = requestContext.getJwtClaims();
        this.orgClaims = getOrgClaim();
    }

    private record OrgClaims(String org, String serviceLevel) {}

    private OrgClaims getOrgClaim() {
        String org = "";
        if (claims.getString(ORG).isPresent()) {
            org = claims.getString(ORG).get();
        }
        if (this.enableOrgServiceLevelSaas) {
            if (org == null) {
                throw HttpException.badRequest(ORG + ORG_NULL_MSG);
            }
        }

        String serviceLevel = SERVICE_LEVEL_FREE;
        if (claims.getString(SERVICE_LEVEL).isPresent()) {
            serviceLevel = claims.getString(SERVICE_LEVEL).get();
        }
        if (log.isDebugEnabled()) {
            log.debug("Org claims: {}, and service level: {}", org, serviceLevel);
        }
        return new OrgClaims(org, serviceLevel.toLowerCase());
    }

    private CompletionStage<Organization> getOrg(String org) {
        return componentClient
                        .forKeyValueEntity(org)
                        .method(OrgEntity::get)
                        .invokeAsync();
    }

    private CompletionStage<Boolean> doesExceedSubscription() {
        if (this.enableOrgServiceLevelSaas) {
            return getOrg(orgClaims.org()).thenApply(org -> {
                return switch (orgClaims.serviceLevel()) {
                    case SERVICE_LEVEL_FREE -> {
                        if (org.totalBytesCached() > freeServiceMaxCachedBytes) {
                            yield true;
                        } else {
                            yield false;
                        }
                    }
                    case SERVICE_LEVEL_GATLING -> false;
                    default -> {
                        throw HttpException.badRequest(String.format(SERVICE_LEVEL_JWT_ERR_MSG, orgClaims.serviceLevel()));
                    }
                };
            });
        }
        else {
            return CompletableFuture.completedFuture(false);
        }
    }

    // Cache Names -- BEGIN
    private CacheNameRequest modCacheNameWithOrg(CacheNameRequest request) {
        return new CacheNameRequest(orgClaims.org.concat(request.cacheName()), request.description());
    }

    @Post("/cacheName")
    public CompletionStage<HttpResponse> createCacheName(CacheNameRequest request) {
        return core.createCacheName(modCacheNameWithOrg(request));
    }

    @Put("/cacheName")
    public CompletionStage<HttpResponse> updateCacheName(CacheNameRequest request) {
        return core.updateCacheName(modCacheNameWithOrg(request));
    }

    @Get("/cacheName/{cacheName}")
    public CompletionStage<CacheName> getCacheName(String cacheName) {
        return core.getCacheName(orgClaims.org.concat(cacheName));
    }

    // This deletes the cacheName as well as all the keys
    @Delete("/cacheName/{cacheName}")
    public CompletionStage<HttpResponse> deleteCacheKeys(String cacheName) {
        return core.deleteCacheKeys(orgClaims.org.concat(cacheName), false);
    }

    // This deletes all the cached data but leaves the cacheName in place
    @Put("/cacheName/{cacheName}/flush")
    public CompletionStage<HttpResponse> flushCacheKeys(String cacheName) {
        return core.deleteCacheKeys(orgClaims.org.concat(cacheName), true);
    }

    // Cache Names -- END

    // Cache API -- BEGIN

    /* this is the JSON version of set */
    private CacheRequest modCachNameCacheRequestWithOrg(CacheRequest cacheRequest) {
        return new CacheRequest(
                Optional.of(orgClaims.org()),
                orgClaims.org.concat(cacheRequest.cacheName()),
                cacheRequest.key(),
                cacheRequest.ttlSeconds(),
                cacheRequest.value()
        );
    }

    @Post("/set")
    public CompletionStage<HttpResponse> cache(CacheRequest cacheRequest) {
        return doesExceedSubscription().thenCompose(isExceeded -> {
           if (isExceeded) {
               return CompletableFuture.completedFuture(HttpResponses.badRequest(EXCEEDED_CACHED_ALLOTMENT));
           }
           return core.cache(modCachNameCacheRequestWithOrg(cacheRequest));
        });
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Post("/{cacheName}/{key}/{ttlSeconds}") 
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, Integer ttlSeconds, HttpEntity.Strict strictRequestBody) {
        return doesExceedSubscription().thenCompose(isExceeded -> {
            if (isExceeded) {
                return CompletableFuture.completedFuture(HttpResponses.badRequest(EXCEEDED_CACHED_ALLOTMENT));
            }
            return core.cacheSet(Optional.of(orgClaims.org()), orgClaims.org.concat(cacheName), key, ttlSeconds, strictRequestBody);
        });
    }

    @Post("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> cacheSet(String cacheName, String key, HttpEntity.Strict strictRequestBody) {
        return doesExceedSubscription().thenCompose(isExceeded -> {
            if (isExceeded) {
                return CompletableFuture.completedFuture(HttpResponses.badRequest(EXCEEDED_CACHED_ALLOTMENT));
            }
            return core.cacheSet(Optional.of(orgClaims.org()), orgClaims.org.concat(cacheName), key, DEFAULT_TTL, strictRequestBody);
        });
    }

    // this is a JSON verison of GET
    @Get("/get/{cacheName}/{key}")
    public CompletionStage<CacheGetResponse> getCache(String cacheName, String key) {
        return core.getCache(orgClaims.org.concat(cacheName), key);
    }

    /*
     This is an alternate binary REST call that is now a default.

     This solves the problem of having to convert into
     and out of ByteString for chunking.
    */
    @Get("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> getCacheGet(String cacheName, String key) {
        return core.getCacheGet(orgClaims.org.concat(cacheName), key);
    }

    @Get("/{cacheName}/keys")
    public CompletionStage<CacheGetKeysResponse> getCacheKeys(String cacheName) {
        return core.getCacheKeys(orgClaims.org.concat(cacheName));
    }

    @Delete("/{cacheName}/{key}")
    public CompletionStage<HttpResponse> delete(String cacheName, String key) {
        return core.delete(orgClaims.org.concat(cacheName), key);
    }

    BatchCacheRequest addOrgToBatchRequest(BatchCacheRequest batchCacheRequest) {
        List<CacheRequest> cacheRequests = new ArrayList<>();
        batchCacheRequest.cacheRequests().forEach(cacheRequest -> {
            cacheRequests.add(modCachNameCacheRequestWithOrg(cacheRequest));
        });
        return new BatchCacheRequest(cacheRequests);
    }

    BatchCacheResponse failAllBatchRequests(BatchCacheRequest batchCacheRequest) {
        List<BatchCacheResult> cacheResults = new ArrayList<>();
        batchCacheRequest.cacheRequests().forEach(cacheRequest -> {
            cacheResults.add(new BatchCacheResult(cacheRequest.cacheName(), cacheRequest.key(), false));
        });
        return new BatchCacheResponse(false, cacheResults);
    }

    @Post("/batch")
    public CompletionStage<BatchCacheResponse> cacheBatch(BatchCacheRequest batchCacheRequest) {
        return doesExceedSubscription().thenCompose(isExceeded -> {
            if (isExceeded) {
                return CompletableFuture.completedFuture(failAllBatchRequests(batchCacheRequest));
            }
            return core.cacheBatch(addOrgToBatchRequest(batchCacheRequest));
        });
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
        return core.getCacheBatch(addOrgToBatchGetRequest(batchGetCacheRequests));
    }

    @Delete("/batch")
    public CompletionStage<BatchDeleteCacheResponse> deleteCacheBatch(BatchGetCacheRequests batchGetCacheRequests) {
        return core.deleteCacheBatch(addOrgToBatchGetRequest(batchGetCacheRequests));
    }

    // Cache API -- END
}
