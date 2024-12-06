package com.akka.cache.domain;

public record Organization(String orgName, Long totalBytesCached) {
    public Organization withIncrementedBytesCached(long incrementBytesBy) {
        return new Organization(orgName, totalBytesCached + incrementBytesBy);
    }

    public Organization withDecrementedBytesCached(long decrementBytesBy) {
        return new Organization(orgName, totalBytesCached - decrementBytesBy);
    }
}
