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
import com.akka.cache.domain.Cache;
import com.akka.cache.domain.CacheName;
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

  public CacheEndpoint(Config config, ComponentClient componentClient, TimerScheduler timerScheduler) {
    this.config = config;
    this.componentClient = componentClient;
    this.timerScheduler = timerScheduler;
    cacheNameNeededFirst = config.getBoolean("app.cache-name-needed-first");
  }


  /*
  TODO: Do we really need this? The only real reason to have a separate cacheName entity is to maintain a description of the cache
   */
  // Cache Names -- BEGIN
  public record CacheNameRequest(String cacheName, String description) {}

  @Post("/cacheName/create")
  public CompletionStage<HttpResponse> create(CacheNameRequest request) {
    CacheName cn = new CacheName(request.cacheName, Optional.of(request.description));
    return componentClient.forKeyValueEntity(cn.cacheName())
            .method(CacheNameEntity::create)
            .invokeAsync(cn)
            .thenApply(__ -> HttpResponses.created());
  }

  // TODO: /cacheName/createBatch

  @Post("/cacheName/update")
  public CompletionStage<HttpResponse> update(CacheNameRequest request) {
    CacheName cn = new CacheName(request.cacheName, Optional.of(request.description));
    return componentClient.forKeyValueEntity(cn.cacheName())
            .method(CacheNameEntity::update)
            .invokeAsync(cn)
            .thenApply(__ -> HttpResponses.accepted());
  }

  @Get("/cacheName/{cacheName}")
  public CompletionStage<CacheName> getCacheName(String cacheName) {
    return componentClient.forKeyValueEntity(cacheName)
            .method(CacheNameEntity::get)
            .invokeAsync()
            .thenApply(c -> c);
  }

  @Get("/cacheName/keys/{cacheName}")
  public CompletionStage<CacheView.CacheSummaries> getCacheKeyList(String cacheName) {
    return componentClient.forView()
            .method(CacheView::getCacheKeys)
            .invokeAsync(cacheName)
            .exceptionally(ex -> {
              throw HttpException.badRequest(String.format("No cached items found for %s", cacheName));
            });
  }

/* TODO: finish Deletes w/ workflow
  @Delete("/cacheName/{cacheName}")
  public CompletionStage<HttpResponse> deleteCacheKeys(String cacheName) {
    CompletionStage<CacheView.CacheSummaries> cacheKeys = componentClient.forView()
            .method(CacheView::getCacheKeys)
            .invokeAsync(cacheName)
            .exceptionally(ex -> {
              throw HttpException.badRequest(String.format("No cached items found for %s", cacheName));
            });
    return cacheKeys.thenCompose(keys -> {
    });
  }
*/

  // TODO: finish flush w/ workflow

  // Cache Names -- END

  // Cache API -- BEGIN

  public record CacheRequest(String cacheName, String key, byte[] value, Optional<Integer> ttlSeconds) {
    public CacheRequest(String cacheName, String key, byte[] value) {
      this(cacheName, key, value, Optional.empty());
    }
  }

  private CompletionStage<HttpResponse> createCacheEntity(String cacheName, String key, Cache cache) {
    String cacheId = String.format("%s%s", cacheName, key);
    var setresult = componentClient.forKeyValueEntity(cacheId)
            .method(CacheEntity::set)
            .invokeAsync(cache)
            .thenApply(result -> HttpResponses.created());

    return setresult.thenCompose(result -> {
      if (!cache.ttlSeconds().isEmpty()) {
        if (log.isDebugEnabled()) {
          log.debug("starting TTL timer for {} {} {}", cacheName, key, cache.ttlSeconds().get().getSeconds());
        }
        CompletionStage<Done> timerRegistration =
                timerScheduler.startSingleTimer(
                        String.format("%s%s", "cached", cacheId),
                        cache.ttlSeconds().get(),
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
      return new Cache(cacheRequest.cacheName, cacheRequest.key, cacheRequest.value);
    }
    else {
      return new Cache(cacheRequest.cacheName, cacheRequest.key, cacheRequest.value,
              Optional.of(Duration.ofSeconds(cacheRequest.ttlSeconds.get())));
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

  // TODO : Create Cache w/ TTL
  // TODO : Create batched Cache
  // TODO : Create batched Cache w/ TTL

  @Get("/{cacheName}/{key}")
  public CompletionStage<Cache> getCache(String cacheName, String key) {
    return componentClient.forKeyValueEntity(String.format("%s%s", cacheName, key))
            .method(CacheEntity::get)
            .invokeAsync()
            .thenApply(c -> c);
  }

  // Cache API -- END
}
