package com.akka.cache.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import com.akka.cache.application.CacheNameEntity;
import com.akka.cache.application.CacheEntity;
import com.akka.cache.application.CacheView;
import com.akka.cache.domain.Cache;
import com.akka.cache.domain.CacheName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/cache")
public class CacheEndpoint {
  private static final Logger log = LoggerFactory.getLogger(CacheEndpoint.class);

  private final ComponentClient componentClient;

  public CacheEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
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

  // TODO: Delete Cache
  // TODO: Flush Cache

  // Cache Names -- END

  // Cache API -- BEGIN

  public record CacheRequest(String cacheName, String key, byte[] value) {}

  @Post("/")
  public CompletionStage<HttpResponse> cache(CacheRequest request) {

    // TODO: Do we really need to validate that cache name exists?
    CompletionStage<CacheName> cName = getCacheName(request.cacheName);
    return cName.thenCompose(c -> {
      if (c.cacheName().isEmpty()) {
        return CompletableFuture.completedFuture(HttpResponses.notFound());
      }
      else {
        Cache cache = Cache.withOutTTL(request.cacheName, request.key, request.value);
        return componentClient.forKeyValueEntity(String.format("%s%s", request.cacheName, request.key))
                .method(CacheEntity::set)
                .invokeAsync(cache)
                .thenApply(result -> HttpResponses.created());
                // TODO clear the TTL timer if it exists
      }
    });
  }

  @Get("/{cacheName}/{key}")
  public CompletionStage<Cache> getCache(String cacheName, String key) {
    return componentClient.forKeyValueEntity(String.format("%s%s", cacheName, key))
            .method(CacheEntity::get)
            .invokeAsync()
            .thenApply(c -> c);
  }

  // Cache API -- END
}
