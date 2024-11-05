package com.akka.cache.application;


import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import com.akka.cache.domain.Cache;
import com.akka.cache.domain.CacheEvent;
import com.akka.cache.domain.PayloadChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static akka.Done.done;

@ComponentId("cache")
public class CacheEntity extends EventSourcedEntity<Cache, CacheEvent> {
    private static final Logger log = LoggerFactory.getLogger(CacheEntity.class);

    public ReadOnlyEffect<Cache> get() {
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        else {
            // TODO: just return the first payload
            return effects().reply(currentState());
        }
    }

    public ReadOnlyEffect<Cache> getChunk(int index) {
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        else {
            // just return the first payload
            return effects().reply(
                    currentState().withChunk(currentState().chunks().get(index))
            );
        }
    }

    public Effect<Done> set(Cache cache) {
        if (log.isDebugEnabled()) {
            log.info("Creating {}", commandContext().entityId());
        }
        return effects()
                .persist(new CacheEvent.CacheSet(cache.cacheName(), cache.key(), cache.ttlSeconds(), cache.chunks().get(0)))
                .thenReply(__ -> done());
    }

    public Effect<Done> setWithChunk(PayloadChunk chunk) {
        if (log.isDebugEnabled()) {
            log.info("adding chuck {} sequence {}", commandContext().entityId(), chunk.sequence());
        }
        return effects()
                .persist(new CacheEvent.ChunkAdded(chunk))
                .thenReply(__ -> done());
    }

    /*
    TODO: we're not actually deleting for now,
     */
    public Effect<Done> delete() {
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        else {
            // just return the first payload
            return effects()
                    .persist(new CacheEvent.CacheDeleted())
                    .thenReply(__ -> done());
        }
    }

    @Override
    public Cache applyEvent(CacheEvent cacheEvent) {
        return switch (cacheEvent) {
            case CacheEvent.CacheSet cache -> new Cache(cache.cacheName(), cache.key(), cache.ttlSeconds()).withChunk(cache.chunk());
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

/*
import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.keyvalueentity.KeyValueEntity;
import com.akka.cache.domain.Cache;

import java.util.Optional;

import static akka.Done.done;

@ComponentId("cache")
public class CacheEntity extends KeyValueEntity<Cache> {

    @Override
    public Cache emptyState() {
        return new Cache("", commandContext().entityId(), null, Optional.empty());
    }

    public Effect<Done> set(Cache newCache) {
        return effects()
                .updateState(newCache)
                .thenReply(done());
    }

    public Effect<Done> delete() {
        return effects()
                .deleteEntity()
                .thenReply(done());
    }

    public Effect<Cache> get() {
        return effects()
                .reply(currentState());
    }

    public Effect<Boolean> isCached() {
        Boolean isCached = currentState().value().equals(emptyState()) ? false : true;
        return effects().reply(isCached);
    }

}
*/
