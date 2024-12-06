package com.akka.cache.domain;

public record Organization(String orgName, int cacheCount, long totalBytesCached) {
    public Organization withIncrementedBytesCached(long incrementBytesBy) {
        return new Organization(orgName, cacheCount + 1, totalBytesCached + incrementBytesBy);
    }

    public Organization withDecrementedBytesCached(long decrementBytesBy) {
        return new Organization(orgName, cacheCount - 1, totalBytesCached - decrementBytesBy);
    }
}
