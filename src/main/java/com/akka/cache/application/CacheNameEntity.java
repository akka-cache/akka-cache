package com.akka.cache.application;


import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.keyvalueentity.KeyValueEntity;
import com.akka.cache.api.CacheEndpoint;
import com.akka.cache.domain.CacheName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static akka.Done.done;

@ComponentId("cacheName")
public class CacheNameEntity extends KeyValueEntity<CacheName> {
    private static final Logger log = LoggerFactory.getLogger(CacheNameEntity.class);

    @Override
    public CacheName emptyState() { return new CacheName("", Optional.empty()); }

    public Effect<Done> create(CacheName cacheName) {
        if (currentState().equals(emptyState())) {
            if (log.isDebugEnabled()) {
                log.info("Creating cache with id '{}'", commandContext().entityId());
            }
            return effects()
                    .updateState(cacheName)
                    .thenReply(Done.done());
        }
        else {
            String errorMsg = String.format("Named cache %s already exists(%s)", cacheName.cacheName(), currentState());
            return effects().error(errorMsg);
        }
    }

    public Effect<Done> update(CacheName cacheName) {
        CacheName newState = currentState().withDescription(cacheName.description().get());
        return effects()
                .updateState(newState)
                .thenReply(Done.done());
    }

    public Effect<Done> delete() {
        if (log.isDebugEnabled()) {
            log.info("Deleting cacheName with id '{}'", commandContext().entityId());
        }
        return effects()
                .deleteEntity()
                .thenReply(done());
    }

    public Effect<CacheName> get() {
        return effects()
                .reply(currentState());
    }

}