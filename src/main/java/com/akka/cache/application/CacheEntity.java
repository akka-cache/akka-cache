package com.akka.cache.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.keyvalueentity.KeyValueEntity;
import com.akka.cache.domain.Cache;
import com.akka.cache.domain.CacheName;

import java.time.Duration;
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
        Boolean isCached = currentState().value() == null ? false : true;
        return effects().reply(isCached);
    }

}