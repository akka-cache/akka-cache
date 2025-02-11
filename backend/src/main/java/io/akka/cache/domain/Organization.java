package io.akka.cache.domain;

public record Organization(int cacheCount, long totalBytesCached) {
    public Organization withIncrementedBytesCached(long incrementBytesBy) {
        return new Organization(cacheCount + 1, totalBytesCached + incrementBytesBy);
    }

    public Organization withDecrementedBytesCached(long decrementBytesBy) {
        return new Organization(cacheCount - 1, totalBytesCached - decrementBytesBy);
    }
}
