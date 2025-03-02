package io.akka.cache.domain;

import java.time.Duration;
import java.util.Optional;

public record CacheInternalGetResponse(Optional<String> org, String cacheName, String key, Optional<Duration> ttlSeconds, Boolean deleted, long totalBytes, int chunks, PayloadChunk firstChunk) {}
