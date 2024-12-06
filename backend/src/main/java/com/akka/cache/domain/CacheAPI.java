package com.akka.cache.domain;

import akka.http.javadsl.model.HttpResponse;
import java.util.List;
import java.util.Optional;

public sealed interface CacheAPI {
    public record BatchCacheRequest(List<CacheRequest> cacheRequests) implements CacheAPI {}

    public record BatchCacheRequestResults(HttpResponse httpResponse, CacheRequest cacheRequest) implements CacheAPI {}

    public record BatchCacheResponse(Boolean complete, List<BatchCacheResult> results) implements CacheAPI {}

    public record BatchCacheResult(String cacheName, String key, Boolean success) implements CacheAPI {}

    public record BatchDeleteCacheResponse(Boolean success, List<CacheDeleteResponse> cacheDeleteResponses) implements CacheAPI {}

    public record BatchGetCacheRequest(String cacheName, String key) implements CacheAPI {}

    public record BatchGetCacheRequests(List<BatchGetCacheRequest> getCachedBatch) implements CacheAPI {}

    public record BatchGetCacheResponse(Boolean complete, List<CacheGetResponse> results) implements CacheAPI {}

    public record CacheDeleteResponse(String cacheName, String key, Boolean success) implements CacheAPI {}

    public record CacheGetKeysResponse(String cacheName, List<String> keys) implements CacheAPI {}

    public record CacheGetResponse(String cacheName, String key, Boolean success, byte[] value) implements CacheAPI {}

    public record CacheNameRequest(String cacheName, String description) implements CacheAPI {}

    public record CacheRequest(Optional<String> org, String cacheName, String key, Optional<Integer> ttlSeconds, byte[] value) implements CacheAPI {

        public CacheRequest(String cacheName, String key, Optional<Integer> ttlSeconds, byte[] value) {
            this(Optional.empty(), cacheName, key, ttlSeconds, value);
        }

        public CacheRequest(String cacheName, String key, byte[] value) {
            this(Optional.empty(), cacheName, key, Optional.empty(), value);
        }

        public CacheRequest(String cacheName, String key, Optional<Integer> ttlSeconds) {
            this(Optional.empty(), cacheName, key, Optional.empty(), null);
        }

        public CacheRequest(Optional<String> org, String cacheName, String key, Optional<Integer> ttlSecs) {
            this(org, cacheName, key, ttlSecs, null);
        }
    }

}
