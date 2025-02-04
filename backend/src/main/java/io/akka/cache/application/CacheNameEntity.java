package io.akka.cache.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import io.akka.cache.domain.CacheName;
import io.akka.cache.domain.CacheNameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static akka.Done.done;

@ComponentId("cacheName")
public class CacheNameEntity extends EventSourcedEntity<CacheName, CacheNameEvent> {
    private static final Logger log = LoggerFactory.getLogger(CacheNameEntity.class);

    public Effect<Done> create(CacheName cacheName) {
        if (log.isDebugEnabled()) {
            log.debug("Creating cache with id {} description {}}", commandContext().entityId(), cacheName.description());
        }
        return effects()
                .persist(new CacheNameEvent.CacheNameCreated(cacheName.cacheName(), cacheName.description()))
                .thenReply(__ -> done());
    }

    public Effect<Done> update(CacheName cacheName) {
        if (log.isDebugEnabled()) {
            log.debug("Updating cacheName with id {} description {}}", commandContext().entityId(), cacheName.description());
        }
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        return effects()
                .persist(new CacheNameEvent.CacheNameChanged(cacheName.description()))
                .thenReply(__ -> done());
    }

    public Effect<Done> delete() {
        if (log.isDebugEnabled()) {
            log.info("Deleting cacheName with id '{}'", commandContext().entityId());
        }
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        return effects()
                .persist(new CacheNameEvent.CacheNameDeleted())
                .thenReply(__ -> done());
    }

    public Effect<CacheName> get() {
        if (currentState() == null || currentState().deleted()) {
            return errorNotFound();
        }
        else {
            // just return the first payload
            return effects().reply(
                    currentState()
            );
        }
    }

    @Override
    public CacheName applyEvent(CacheNameEvent cacheNameEvent) {
        return switch (cacheNameEvent) {
            case CacheNameEvent.CacheNameCreated created -> new CacheName(created.cacheName(), created.description());
            case CacheNameEvent.CacheNameChanged nameChanged -> currentState().withDescription(nameChanged.description());
            case CacheNameEvent.CacheNameDeleted deleted -> currentState().asDeleted();
        };
    }

    private <T> ReadOnlyEffect<T> errorNotFound() {
        return effects().error(
                "No cache name exists for id " + commandContext().entityId()
        );
    }
}