package com.akka.cache.api;

import akka.Done;
import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.*;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import akka.javasdk.timer.TimerScheduler;
import com.akka.cache.application.CacheNameEntity;
import com.akka.cache.application.CacheEntity;
import com.akka.cache.application.CacheView;
import com.akka.cache.application.CacheTimedAction;
import com.akka.cache.application.CacheNameDeleteWorkflow;
import com.akka.cache.domain.Cache;
import com.akka.cache.domain.CacheName;
import com.akka.cache.domain.PayloadChunk;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/cache")
public class CacheEndpoint {
  private static final Logger log = LoggerFactory.getLogger(CacheEndpoint.class);

  private final Config config;
  private final ComponentClient componentClient;
  private final boolean cacheNameNeededFirst;
  private final TimerScheduler timerScheduler;
  private final Optional<Duration> defaultTTL;

  public CacheEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler) {
    this.config = config;
    this.componentClient = componentClient;
    this.timerScheduler = timerScheduler;
    cacheNameNeededFirst = this.config.getBoolean("app.cache-name-needed-first");
    if (config.hasPath("app.default-default-ttl")) {
      defaultTTL = Optional.of(this.config.getDuration("app.default-default-ttl"));
    }
    else {
      defaultTTL = Optional.empty();
    }
  }


  /*
  Note: The only real reason to have a separate cacheName entity is to maintain a description of the cache
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

  // TODO: /cacheName/createBatch

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

  private CompletionStage<HttpResponse> createCacheEntity(String cacheName, String key, Cache cache) {
    String cacheId = cacheName.concat(key);
    var setresult = componentClient.forEventSourcedEntity(cacheId)
            .method(CacheEntity::set)
            .invokeAsync(cache)
            .thenApply(result -> HttpResponses.created());

    return setresult.thenCompose(result -> {
      Optional<Duration> ttlSeconds = Optional.empty();
      if (!cache.ttlSeconds().isEmpty()) {
        ttlSeconds = cache.ttlSeconds();
      }
      else if (!defaultTTL.isEmpty()) {
        ttlSeconds = defaultTTL;
      }
      if (!ttlSeconds.isEmpty()) {
        if (log.isDebugEnabled()) {
          log.debug("starting TTL timer for {} {} {}", cacheName, key, ttlSeconds.get().getSeconds());
        }
        CompletionStage<Done> timerRegistration =
                timerScheduler.startSingleTimer(
                        "keys".concat(cacheId),
                        ttlSeconds.get(),
                        componentClient.forTimedAction()
                                .method(CacheTimedAction::expireCacheTTL)
                                .deferred(cacheId)
                );
        return timerRegistration.thenApply(__ -> result);
      }
      else {
        return CompletableFuture.completedFuture(result);
      }
    });

  }

  private Cache createCacheObject(CacheRequest cacheRequest) {
    if (cacheRequest.ttlSeconds.isEmpty()) {
      return new Cache(cacheRequest.cacheName, cacheRequest.key).withChunk(new PayloadChunk(0, cacheRequest.value));
    }
    else {
      return new Cache(cacheRequest.cacheName, cacheRequest.key, Optional.of(Duration.ofSeconds(cacheRequest.ttlSeconds.get())))
              .withChunk(new PayloadChunk(0, cacheRequest.value));
    }
  }

  @Post("/")
  public CompletionStage<HttpResponse> cache(CacheRequest request) {
    if (cacheNameNeededFirst) {
      CompletionStage<CacheName> cName = getCacheName(request.cacheName);
      return cName.thenCompose(c -> {
        if (c.cacheName().isEmpty()) {
          return CompletableFuture.completedFuture(HttpResponses.notFound());
        } else {
          return createCacheEntity(request.cacheName, request.key, createCacheObject(request));
        }
      });
    }
    else {
      return createCacheEntity(request.cacheName, request.key, createCacheObject(request));
    }
  }

  // TODO : Create batched Cache
  // TODO : Create batched Cache w/ TTL

  @Get("/{cacheName}/{key}")
  public CompletionStage<Cache> getCache(String cacheName, String key) {
    return componentClient.forEventSourcedEntity(cacheName.concat(key))
            .method(CacheEntity::get)
            .invokeAsync()
            .thenApply(c -> c);
  }

  // Cache API -- END
}
