package com.akka.cache.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.DeleteHandler;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import com.akka.cache.domain.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ComponentId("cache_keys_view")
public class CacheView extends View {

    private static final Logger log = LoggerFactory.getLogger(CacheView.class);

    public record CacheSummaries(List<CacheSummary> cached) {}
    public record CacheSummary(String cacheName, String key) {}

    @Consume.FromKeyValueEntity(CacheEntity.class)
    public static class KeysByCacheName extends TableUpdater<CacheSummary> {
        public Effect<CacheSummary> onUpdate(Cache cache) {
            log.info("CacheView onUpdate received for {} {}", cache.cacheName(), cache.key());
            return effects()
                    .updateRow(new CacheSummary(cache.cacheName(), cache.key()));
        }

        @DeleteHandler
        public Effect<CacheSummary> onDelete() {
            // TODO: clear cache timers for each deleted value if one exists
            log.info("CacheView onDelete received for {}", updateContext().eventSubject());
            return effects().deleteRow();
        }
    }

//    @Query("SELECT cacheName, key FROM cache_view WHERE key LIKE :cacheName")
    @Query("SELECT (cacheName, key) AS cached FROM cache_keys_view WHERE cacheName = :cacheName")
    public QueryEffect<CacheSummaries> getCacheKeys(String cacheName) {
        return queryResult();
    }

}