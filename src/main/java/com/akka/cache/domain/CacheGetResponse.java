package com.akka.cache.domain;

public record CacheGetResponse(String cacheName, String key, byte[] value) {}
