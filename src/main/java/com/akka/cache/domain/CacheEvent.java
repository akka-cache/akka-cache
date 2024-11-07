package com.akka.cache.domain;

import akka.javasdk.annotations.TypeName;

import java.time.Duration;
import java.util.Optional;

public sealed interface CacheEvent {
    @TypeName("cache-set")
    record CacheSet(String cacheName, String key, Optional<Duration> ttlSeconds, PayloadChunk chunk) implements CacheEvent {}

    @TypeName("chunk-added")
    record ChunkAdded(PayloadChunk chunk) implements CacheEvent {}

    @TypeName("cache-deleted")
    record CacheDeleted() implements CacheEvent {}
}
