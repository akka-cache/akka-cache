package com.akka.cache.domain;

import java.util.Optional;

public record CacheName(String cacheName, Optional<String> description, Boolean deleted) {

    public CacheName(String cacheName, Optional<String> description) {
        this(cacheName, description, false);
    }

    public CacheName withName(String cacheName) {
        return new CacheName(cacheName, Optional.empty(), deleted);
    }

    public CacheName withDescription(Optional<String> newDescription) { // <2>
        return new CacheName(cacheName, newDescription, deleted);
    }

    public CacheName withNameOnly(String newCacheName) { // <2>
        return new CacheName(newCacheName, Optional.empty(), deleted);
    }

    public CacheName asDeleted() { // <2>
        return new CacheName(cacheName, description, true);
    }


}
