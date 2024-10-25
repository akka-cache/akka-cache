package com.akka.cache.domain;

import com.akka.cache.application.CacheView;

import static com.akka.cache.domain.DeleteCacheNameState.DeleteStatus.STARTED;

public record DeleteCacheNameState(CacheView.CacheSummaries cached, CacheView.CacheSummaries deleted, DeleteStatus deleteStatus) {

    public enum DeleteStatus {
        STARTED, DELETED_SUCCESSFULLY, DELETE_FAILED
    }

    public DeleteCacheNameState(CacheView.CacheSummaries cached, CacheView.CacheSummaries deleted) {
        this(cached, deleted, STARTED);
    }

    public DeleteCacheNameState withStatus(DeleteStatus newStatus) {
        return new DeleteCacheNameState(cached, deleted, newStatus);
    }
}
