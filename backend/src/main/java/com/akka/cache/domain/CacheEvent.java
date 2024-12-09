package com.akka.cache.domain;

import akka.javasdk.annotations.TypeName;

import java.time.Duration;
import java.util.Optional;

public sealed interface CacheEvent {
    @TypeName("cache-set")
    record CacheSet(Optional<String> org, String cacheName, String key, Optional<Duration> ttlSeconds, long totalBytes, PayloadChunk chunk) implements CacheEvent {}

    @TypeName("chunk-added")
    record ChunkAdded(PayloadChunk chunk) implements CacheEvent {}

    @TypeName("cache-deleted")
    record CacheDeleted(Optional<String> org, long totalBytes) implements CacheEvent {}
}
