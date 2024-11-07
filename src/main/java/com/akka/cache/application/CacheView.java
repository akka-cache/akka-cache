package com.akka.cache.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.akka.cache.domain.CacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ComponentId("cache_keys_view")
public class CacheView extends View {

    private static final Logger log = LoggerFactory.getLogger(CacheView.class);

    public record CacheSummaries(List<CacheSummary> cached) {}
    public record CacheSummary(String cacheName, String key) {}
    public record CachedKeys(List<String> keys) {}

    @Consume.FromEventSourcedEntity(CacheEntity.class)
    public static class KeysByCacheName extends TableUpdater<CacheSummary> {
        public Effect<CacheSummary> onEvent(CacheEvent cacheEvent) {
            return switch (cacheEvent) {
                case CacheEvent.CacheSet cache -> {
                    if (log.isDebugEnabled()) {
                        log.debug("CacheView CacheSet received for {} {}", cache.cacheName(), cache.key());
                    }
                    yield effects()
                            .updateRow(new CacheSummary(cache.cacheName(), cache.key()));
                }
                case CacheEvent.ChunkAdded chunk -> effects().ignore();
                case CacheEvent.CacheDeleted deleted -> effects().deleteRow();
            };
        }
    }

    @Query("SELECT (cacheName, key) AS cached FROM cache_keys_view WHERE cacheName = :cacheName")
    public QueryEffect<CacheSummaries> getCacheSummaries(String cacheName) {
        return queryResult();
    }

    @Query("SELECT key AS keys FROM cache_keys_view WHERE cacheName = :cacheName")
    public QueryEffect<CachedKeys> getCacheKeys(String cacheName) {
        return queryResult();
    }

}

/*    public static class KeysByCacheName extends TableUpdater<CacheSummary> {
        public Effect<CacheSummary> onUpdate(Cache cache) {
            if (log.isDebugEnabled()) {
                log.debug("CacheView onUpdate received for {} {}", cache.cacheName(), cache.key());
            }
            return effects()
                    .updateRow(new CacheSummary(cache.cacheName(), cache.key()));
        }

        @DeleteHandler
        public Effect<CacheSummary> onDelete() {
            if (log.isDebugEnabled()) {
                log.debug("CacheView onDelete received for {}", updateContext().eventSubject());
            }
            return effects().deleteRow();
        }
    }*/