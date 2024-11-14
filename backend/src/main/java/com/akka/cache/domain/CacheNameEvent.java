package com.akka.cache.domain;

import akka.javasdk.annotations.TypeName;

import java.time.Duration;
import java.util.Optional;

public sealed interface CacheNameEvent {
    @TypeName("cache-name-created")
    record CacheNameCreated(String cacheName, Optional<String> description) implements CacheNameEvent {}

    @TypeName("cache-name-description-changed")
    record CacheNameChanged(Optional<String> description) implements CacheNameEvent {}

    @TypeName("cache-name-deleted")
    record CacheNameDeleted() implements CacheNameEvent {}
}
