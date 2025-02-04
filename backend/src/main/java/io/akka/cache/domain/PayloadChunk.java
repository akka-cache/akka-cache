package io.akka.cache.domain;

public record PayloadChunk(Integer sequence, byte[] payload) {
}