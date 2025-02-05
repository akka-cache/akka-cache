package io.akka.cache.application;


import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import io.akka.cache.domain.Cache;
import io.akka.cache.domain.CacheEvent;
import io.akka.cache.domain.CacheInternalGetResponse;
import io.akka.cache.domain.PayloadChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static akka.Done.done;

@ComponentId("cache")
public class CacheEntity extends EventSourcedEntity<Cache, CacheEvent> {
    private static final Logger log = LoggerFactory.getLogger(CacheEntity.class);

    public ReadOnlyEffect<CacheInternalGetResponse> get() {
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        else {
            return effects().reply(new CacheInternalGetResponse(
                    currentState().org(),
                    currentState().cacheName(),
                    currentState().key(),
                    currentState().ttlSeconds(),
                    currentState().deleted(),
                    currentState().totalBytes(),
                    currentState().chunks().size(),
                    currentState().chunks().getFirst())
            );
        }
    }

    public ReadOnlyEffect<PayloadChunk> getChunk(int index) {
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("CacheEntity {} getChunk({})", commandContext().entityId(), index);
            }
            return effects().reply(
                    currentState().chunks().get(index)
            );
        }
    }

    public Effect<Done> set(Cache cache) {
        if (log.isDebugEnabled()) {
            log.debug("CacheEntity Creating new cache for org {} key {}", cache.org(), commandContext().entityId());
        }
        if (currentState() == null || currentState().org().isEmpty() || currentState().deleted()) {
            return effects()
                    .persist(new CacheEvent.CacheSet(cache.org(), cache.cacheName(), cache.key(), cache.ttlSeconds(), cache.totalBytes(), cache.chunks().getFirst()))
                    .thenReply(__ -> done());
        }
        else {
            // we need to make sure to delete the exsiting payload first so that we back out an existing cache for the org
            return effects()
                    .persist(
                            new CacheEvent.CacheDeleted(currentState().org(), currentState().totalBytes()),
                            new CacheEvent.CacheSet(cache.org(), cache.cacheName(), cache.key(), cache.ttlSeconds(), cache.totalBytes(), cache.chunks().getFirst())
                    )
                    .thenReply(__ -> done());
        }
    }

    public Effect<Done> setWithChunk(PayloadChunk chunk) {
        if (log.isDebugEnabled()) {
            log.debug("CacheEntity adding chunk org {} id {} sequence {}", currentState().org(), commandContext().entityId(), chunk.sequence());
        }
        return effects()
                .persist(new CacheEvent.ChunkAdded(chunk))
                .thenReply(__ -> done());
    }

    /*
    TODO: we're not actually deleting for now, just flagging it
     */
    public Effect<Done> delete() {
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("CacheEntity delete for org {} {}", currentState().org(), commandContext().entityId());
            }
            return effects()
                    .persist(new CacheEvent.CacheDeleted(currentState().org(), currentState().totalBytes()))
                    .thenReply(__ -> done());
        }
    }

    @Override
    public Cache applyEvent(CacheEvent cacheEvent) {
        return switch (cacheEvent) {
            case CacheEvent.CacheSet cache -> new Cache(cache.org(), cache.cacheName(), cache.key(), cache.ttlSeconds(), cache.totalBytes(), Arrays.asList(cache.chunk()));
            case CacheEvent.ChunkAdded chunk -> currentState().withChunk(chunk.chunk());
            case CacheEvent.CacheDeleted deleted -> currentState().asDeleted();
        };
    }

    private <T> ReadOnlyEffect<T> errorNotFound() {
        return effects().error(
                "No cache exists for id " + commandContext().entityId()
        );
    }
}