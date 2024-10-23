package com.akka.cache.domain;

import java.util.Optional;

public record CacheName(String cacheName, Optional<String> description) {

    public CacheName withName(String cacheName) {
        return new CacheName(cacheName, Optional.empty());
    }

    public CacheName withDescription(String newDescription) { // <2>
        return new CacheName(cacheName, Optional.of(newDescription));
    }

    public CacheName withNameOnly(String newCacheName) { // <2>
        return new CacheName(newCacheName, Optional.empty());
    }

}
