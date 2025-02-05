package io.akka.cache.domain;

import io.akka.cache.application.CacheView;

import java.util.Collections;

import static io.akka.cache.domain.DeleteCacheNameState.DeleteStatus.INPROGRESS;

public record DeleteCacheNameState(String cacheName, CacheView.CachedKeys keys, Integer currOffset, DeleteStatus deleteStatus, Integer intRetries, Boolean flushOnly) {
    public DeleteCacheNameState(String cacheName, Boolean flushOnly) {
        this(cacheName, new CacheView.CachedKeys(Collections.emptyList()), 0, DeleteStatus.EMPTY, 0, flushOnly);
    }

    public enum DeleteStatus {
        EMPTY, INPROGRESS, COMPLETE
    }

    public DeleteCacheNameState withStatus(DeleteStatus newStatus) {
        return new DeleteCacheNameState(cacheName, keys, currOffset, newStatus, intRetries, flushOnly);
    }

    public DeleteCacheNameState withCached(CacheView.CachedKeys cached) {
        return new DeleteCacheNameState(cacheName, cached, currOffset, INPROGRESS, intRetries, flushOnly);
    }

    public DeleteCacheNameState withCurrOffset(Integer currOffset) {
        return new DeleteCacheNameState(cacheName, keys, currOffset, deleteStatus, intRetries, flushOnly);
    }

    public DeleteCacheNameState withIntRetries(Integer intRetries) {
        return new DeleteCacheNameState(cacheName, keys, currOffset, deleteStatus, intRetries, flushOnly);
    }

}
