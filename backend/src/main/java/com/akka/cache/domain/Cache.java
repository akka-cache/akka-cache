package com.akka.cache.domain;

import java.time.Duration;
import java.util.*;

public record Cache(String cacheName, String key, Optional<Duration> ttlSeconds, Boolean deleted, long totalBytes, Boolean chunked, List<PayloadChunk> chunks) {
    public Cache(String cacheName, String key, Optional<Duration> ttlSeconds, long totalBytes) {
        this(cacheName, key, ttlSeconds, false, totalBytes, false, Collections.emptyList());;
    }

    public Cache(String cacheName, String key, Optional<Duration> ttlSeconds, long totalBytes, List<PayloadChunk> chunks) {
        this(cacheName, key, ttlSeconds, false, totalBytes, true, chunks);
    }

    public Cache(String cacheName, String key, long totalBytes, List<PayloadChunk> chunks) {
        this(cacheName, key, Optional.empty(), false, totalBytes, true, chunks);
    }

    public Cache(String cacheName, String key) {
        this(cacheName, key, Optional.empty(), false, 0l, false, Collections.emptyList());;
    }

    public Cache withChunk(PayloadChunk chunk) {
        List<PayloadChunk> newChunks = new ArrayList<>(chunks);
        newChunks.add(chunk); // I hate this doesn't return a new list
        return new Cache(cacheName, key, ttlSeconds, deleted, totalBytes, chunked, newChunks);
    }

    public Cache asDeleted() {
        return new Cache(cacheName, key, ttlSeconds, true, totalBytes, chunked, chunks);
    }
}

// TODO: may need to carry a type of the value (payload) if we want type specific endpoint APIs