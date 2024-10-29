package com.akka.cache.domain;

import com.akka.cache.application.CacheView;

import java.util.Collections;

import static com.akka.cache.domain.DeleteCacheNameState.DeleteStatus.DELETED;
import static com.akka.cache.domain.DeleteCacheNameState.DeleteStatus.INPROGRESS;

public record DeleteCacheNameState(String cacheName, CacheView.CachedKeys keys, CacheView.CachedKeys deleted, Integer killTTLPos, DeleteStatus deleteStatus) {
    public DeleteCacheNameState(String cacheName) {
        this(cacheName, new CacheView.CachedKeys(Collections.emptyList()), new CacheView.CachedKeys(Collections.emptyList()), 0, DeleteStatus.EMPTY);
    }

    public enum DeleteStatus {
        EMPTY, INPROGRESS, DELETED, COMPLETE
    }

    public DeleteCacheNameState withStatus(DeleteStatus newStatus) {
        return new DeleteCacheNameState(cacheName, keys, deleted, killTTLPos, newStatus);
    }

    public DeleteCacheNameState withCached(CacheView.CachedKeys cached) {
        return new DeleteCacheNameState(cacheName, cached, deleted, killTTLPos, INPROGRESS);
    }

    public DeleteCacheNameState withDeleted(CacheView.CachedKeys deleted) {
        if ((keys.keys().size() == deleted.keys().size())) {
            return new DeleteCacheNameState(cacheName, keys, deleted, killTTLPos, DELETED);
        }
        return new DeleteCacheNameState(cacheName, keys, deleted, killTTLPos, INPROGRESS);
    }

    public DeleteCacheNameState withKillTTLPos(Integer killTTLPos) {
        return new DeleteCacheNameState(cacheName, keys, deleted, killTTLPos, deleteStatus);
    }
}
