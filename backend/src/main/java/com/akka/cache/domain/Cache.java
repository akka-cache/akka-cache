package com.akka.cache.domain;

import akka.javasdk.annotations.TypeName;

import java.time.Duration;
import java.util.*;

public record Cache(Optional<String> org, String cacheName, String key, Optional<Duration> ttlSeconds, Boolean deleted, long totalBytes, Boolean chunked, List<PayloadChunk> chunks) {
    public Cache(Optional<String> org, String cacheName, String key, Optional<Duration> ttlSeconds, long totalBytes, List<PayloadChunk> chunks) {
        this(org, cacheName, key, ttlSeconds, false, totalBytes, false, chunks);
    }

    public Cache(String cacheName, String key, Optional<Duration> ttlSeconds, Boolean deleted, long totalBytes, Boolean chunked, List<PayloadChunk> chunks) {
        this(Optional.empty(), cacheName, key, ttlSeconds, deleted, totalBytes, chunked, chunks);
    }

    public Cache(String cacheName, String key, Optional<Duration> ttlSeconds, long totalBytes) {
        this(Optional.empty(), cacheName, key, ttlSeconds, false, totalBytes, false, Collections.emptyList());
    }

    public Cache(String cacheName, String key, Optional<Duration> ttlSeconds, long totalBytes, List<PayloadChunk> chunks) {
        this(Optional.empty(), cacheName, key, ttlSeconds, false, totalBytes, true, chunks);
    }

    public Cache(String cacheName, String key, long totalBytes, List<PayloadChunk> chunks) {
        this(Optional.empty(), cacheName, key, Optional.empty(), false, totalBytes, true, chunks);
    }

    public Cache(String cacheName, String key) {
        this(Optional.empty(), cacheName, key, Optional.empty(), false, 0L, false, Collections.emptyList());
    }

    public Cache withChunk(PayloadChunk chunk) {
        List<PayloadChunk> newChunks = new ArrayList<>(chunks);
        newChunks.add(chunk); // I hate this doesn't return a new list
        return new Cache(Optional.empty(), cacheName, key, ttlSeconds, deleted, totalBytes, chunked, newChunks);
    }

    public Cache withOrg(String org) {
        return new Cache(Optional.of(org), cacheName, key, ttlSeconds, deleted, totalBytes, chunked, chunks);
    }

    public Cache asDeleted() {
        return new Cache(Optional.empty(), cacheName, key, ttlSeconds, true, totalBytes, chunked, chunks);
    }
}