package com.akka.cache.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.keyvalueentity.KeyValueEntity;
import com.akka.cache.domain.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static akka.Done.done;

@ComponentId("organization")
public class OrgEntity extends KeyValueEntity<Organization> {
    private static final Logger log = LoggerFactory.getLogger(OrgEntity.class);

    @Override
    public Organization emptyState() {
        return new Organization(commandContext().entityId(), 0, 0L);
    }

    // this can be used to "reset" total usage
    public Effect<Done> set(String orgName) {
        if (log.isDebugEnabled()) {
            log.debug("OrgEntity set received for {}", orgName);
        }
        return effects()
                .updateState(new Organization(orgName, 0, 0L))
                .thenReply(done());
    }

    // not sure if we need this
    public Effect<Done> delete() {
        if (log.isDebugEnabled()) {
            log.debug("OrgEntity set received for {}", currentState().orgName());
        }
        return effects()
                .deleteEntity()
                .thenReply(done());
    }

    public Effect<Organization> get() {
        if (log.isDebugEnabled()) {
            log.debug("OrgEntity get() received for {} total bytes cached {}", currentState().orgName(), currentState().totalBytesCached());
        }
        return effects()
                .reply(currentState());
    }

    public Effect<Done> increment(long cacheSize) {
        if (log.isDebugEnabled()) {
            log.debug("OrgEntity increment of {} received for {} total bytes cached {}", cacheSize, currentState().orgName(), currentState().totalBytesCached());
        }
        return effects()
                .updateState(currentState().withIncrementedBytesCached(cacheSize))
                .thenReply(done());
    }

    public Effect<Done> decrement(long cacheSize) {
        if (log.isDebugEnabled()) {
            log.debug("OrgEntity decrement of {} received for {} total bytes cached {}", cacheSize, currentState().orgName(), currentState().totalBytesCached());
        }
        return effects()
                .updateState(currentState().withDecrementedBytesCached(cacheSize))
                .thenReply(done());
    }

}
