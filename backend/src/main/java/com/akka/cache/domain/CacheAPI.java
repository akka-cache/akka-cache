package com.akka.cache.domain;

import akka.http.javadsl.model.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public sealed interface CacheAPI {

    public record BatchCacheRequest(List<CacheRequest> cacheRequests) implements CacheAPI {

        // Sanitize the input during construction
        public BatchCacheRequest(List<CacheRequest> cacheRequests) {
            this.cacheRequests = cacheRequests == null || cacheRequests.stream().anyMatch(Objects::isNull)
                    ? Collections.emptyList()
                    : List.copyOf(cacheRequests);
        }
    }

    public record BatchCacheRequestResults(HttpResponse httpResponse, CacheRequest cacheRequest) implements CacheAPI {

        // Sanitize the inputs during construction
        public BatchCacheRequestResults(HttpResponse httpResponse, CacheRequest cacheRequest) {
            this.httpResponse = Objects.requireNonNullElse(httpResponse, HttpResponse.create()); // Use a default or placeholder
            this.cacheRequest = Objects.requireNonNullElse(cacheRequest, CacheRequest.EMPTY_REQUEST); // Use a default or placeholder
        }
    }

    public record BatchCacheResponse(Boolean complete, List<BatchCacheResult> results) implements CacheAPI {

        // Sanitize the inputs during construction
        public BatchCacheResponse(Boolean complete, List<BatchCacheResult> results) {
            // Ensure `complete` is not null; default to `false` if null
            this.complete = Objects.requireNonNullElse(complete, false);
            
            // Ensure `results` is not null or containing null elements; default to an empty list if invalid
            this.results = results == null || results.stream().anyMatch(Objects::isNull)
                    ? Collections.emptyList()
                    : List.copyOf(results);
        }
    }

    public record BatchCacheResult(String cacheName, String key, Boolean success) implements CacheAPI {

        // Sanitize the inputs during construction
        public BatchCacheResult(String cacheName, String key, Boolean success) {
            // Ensure `cacheName` is not null; default to an empty string if null
            this.cacheName = Objects.requireNonNullElse(cacheName, "");
    
            // Ensure `key` is not null; default to an empty string if null
            this.key = Objects.requireNonNullElse(key, "");
    
            // Ensure `success` is not null; default to `false` if null
            this.success = Objects.requireNonNullElse(success, false);
        }
    }

    public record BatchDeleteCacheResponse(Boolean success, List<CacheDeleteResponse> cacheDeleteResponses) implements CacheAPI {

        // Sanitize the inputs during construction
        public BatchDeleteCacheResponse(Boolean success, List<CacheDeleteResponse> cacheDeleteResponses) {
            // Ensure `success` is not null; default to `false` if null
            this.success = Objects.requireNonNullElse(success, false);
    
            // Ensure `cacheDeleteResponses` is not null or contains no null elements
            this.cacheDeleteResponses = cacheDeleteResponses == null || cacheDeleteResponses.stream().anyMatch(Objects::isNull)
                    ? Collections.emptyList()
                    : List.copyOf(cacheDeleteResponses);
        }
    }

    public record BatchGetCacheRequest(String cacheName, String key) implements CacheAPI {

        // Sanitize the inputs during construction
        public BatchGetCacheRequest(String cacheName, String key) {
            // Ensure `cacheName` is not null; default to an empty string if null
            this.cacheName = Objects.requireNonNullElse(cacheName, "");
    
            // Ensure `key` is not null; default to an empty string if null
            this.key = Objects.requireNonNullElse(key, "");
        }
    }
   
    public record BatchGetCacheRequests(List<BatchGetCacheRequest> getCachedBatch) implements CacheAPI {

        // Sanitize the inputs during construction
        public BatchGetCacheRequests(List<BatchGetCacheRequest> getCachedBatch) {
            // Ensure `getCachedBatch` is not null or contains null elements
            this.getCachedBatch = getCachedBatch == null || getCachedBatch.stream().anyMatch(Objects::isNull)
                    ? Collections.emptyList()
                    : List.copyOf(getCachedBatch);
        }
    }

    public record BatchGetCacheResponse(Boolean complete, List<CacheGetResponse> results) implements CacheAPI {

        // Sanitize the inputs during construction
        public BatchGetCacheResponse(Boolean complete, List<CacheGetResponse> results) {
            // Ensure `complete` is not null; default to `false` if null
            this.complete = Objects.requireNonNullElse(complete, false);
    
            // Ensure `results` is not null or contains no null elements
            this.results = results == null || results.stream().anyMatch(Objects::isNull)
                    ? Collections.emptyList()
                    : List.copyOf(results);
        }
    }

    public record CacheDeleteResponse(String cacheName, String key, Boolean success) implements CacheAPI {

        // Sanitize the inputs during construction
        public CacheDeleteResponse(String cacheName, String key, Boolean success) {
            // Ensure `cacheName` is not null; default to an empty string if null
            this.cacheName = Objects.requireNonNullElse(cacheName, "");
    
            // Ensure `key` is not null; default to an empty string if null
            this.key = Objects.requireNonNullElse(key, "");
    
            // Ensure `success` is not null; default to `false` if null
            this.success = Objects.requireNonNullElse(success, false);
        }
    }

    public record CacheGetKeysResponse(String cacheName, List<String> keys) implements CacheAPI {

        // Sanitize the inputs during construction
        public CacheGetKeysResponse(String cacheName, List<String> keys) {
            // Ensure `cacheName` is not null; default to an empty string if null
            this.cacheName = Objects.requireNonNullElse(cacheName, "");
    
            // Ensure `keys` is not null or contains no null elements
            this.keys = keys == null || keys.stream().anyMatch(Objects::isNull)
                    ? Collections.emptyList()
                    : List.copyOf(keys);
        }
    }
   
    public record CacheGetResponse(String cacheName, String key, Boolean success, byte[] value) implements CacheAPI {

        // Sanitize the inputs during construction
        public CacheGetResponse(String cacheName, String key, Boolean success, byte[] value) {
            // Ensure `cacheName` is not null; default to an empty string if null
            this.cacheName = Objects.requireNonNullElse(cacheName, "");
    
            // Ensure `key` is not null; default to an empty string if null
            this.key = Objects.requireNonNullElse(key, "");
    
            // Ensure `success` is not null; default to `false` if null
            this.success = Objects.requireNonNullElse(success, false);
    
            // Ensure `value` is not null; default to an empty byte array if null
            this.value = value == null ? new byte[0] : value.clone(); // Defensive copy for immutability
        }
    }

    public record CacheNameRequest(String cacheName, String description) implements CacheAPI {

        // Sanitize the inputs during construction
        public CacheNameRequest(String cacheName, String description) {
            // Ensure `cacheName` is not null; default to an empty string if null
            this.cacheName = Objects.requireNonNullElse(cacheName, "");
    
            // Ensure `description` is not null; default to an empty string if null
            this.description = Objects.requireNonNullElse(description, "");
        }
    }

    public record CacheRequest(Optional<String> org, String cacheName, String key, Optional<Integer> ttlSeconds, byte[] value) implements CacheAPI {

        public static final CacheRequest EMPTY_REQUEST = new CacheRequest(Optional.empty(), "", "", Optional.empty(), new byte[0]);

        // Sanitize the inputs during construction
        public CacheRequest(Optional<String> org, String cacheName, String key, Optional<Integer> ttlSeconds, byte[] value) {
            // Ensure `org` is not null; default to Optional.empty() if null
            this.org = org == null ? Optional.empty() : org;

            // Ensure `cacheName` is not null; default to an empty string if null
            this.cacheName = Objects.requireNonNullElse(cacheName, "");

            // Ensure `key` is not null; default to an empty string if null
            this.key = Objects.requireNonNullElse(key, "");

            // Ensure `ttlSeconds` is not null; default to Optional.empty() if null
            this.ttlSeconds = ttlSeconds == null ? Optional.empty() : ttlSeconds;

            // Ensure `value` is not null; default to an empty byte array if null
            this.value = value == null ? new byte[0] : value.clone(); // Defensive copy for immutability
        }

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
