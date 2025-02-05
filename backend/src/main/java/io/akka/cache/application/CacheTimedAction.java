package io.akka.cache.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timedaction.TimedAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("cache-timed-action")
public class CacheTimedAction extends TimedAction {
    private static final Logger log = LoggerFactory.getLogger(CacheTimedAction.class);

    private final ComponentClient componentClient;

    public CacheTimedAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect expireCacheTTL(String cacheId) {
        if (log.isDebugEnabled()) {
            log.debug("CacheTimedAction expiring cache for cacheId {}", cacheId);
        }
        return effects().asyncDone(
                componentClient.forEventSourcedEntity(cacheId)
                        .method(CacheEntity::delete)
                        .invokeAsync()
                        .thenApply(__ -> Done.done()));
    }

}
