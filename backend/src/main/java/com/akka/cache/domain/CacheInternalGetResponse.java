package com.akka.cache.domain;

import java.time.Duration;
import java.util.Optional;

public record CacheInternalGetResponse(String cacheName, String key, Optional<Duration> ttlSeconds, Boolean deleted, long totalBytes, int chunks, PayloadChunk firstChunk) {}
