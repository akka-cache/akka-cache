package com.akka.cache.domain;

import java.time.Duration;
import java.util.Optional;

public record Cache(String cacheName, String key, byte[] value, Optional<Duration> ttlSeconds) {
    public Cache(String cacheName, String key, byte[] value) {
        this(cacheName, key, value, Optional.empty());
    }
}

// TODO: may need to carry a type of the value (payload)