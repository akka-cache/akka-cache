package io.akka.cache.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import io.akka.cache.domain.CacheEvent;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ComponentId("cached-events-consumer")
@Consume.FromEventSourcedEntity(CacheEntity.class)
public class CacheConsumer extends Consumer {
    private static final Logger log = LoggerFactory.getLogger(CacheConsumer.class);

    final ComponentClient componentClient;
    final boolean enableOrgServiceLevelSaas;

    public CacheConsumer(Config config, ComponentClient componentClient) {
        this.componentClient = componentClient;
        this.enableOrgServiceLevelSaas = config.getBoolean("app.enable-org-service-level-saas");
    }

    public Effect onEvent(CacheEvent event) {
        /*
            TODO: replace this configuration with @Profile("saas") to disable
            this Consumer and OrgEntity when avaialble in the SDK.
        */
        if (enableOrgServiceLevelSaas) {
            return switch (event) {
                case CacheEvent.CacheSet increaseBytesUsed -> {
                    if (increaseBytesUsed.org().isPresent()) {
                        if (log.isDebugEnabled()) {
                            Optional<String> cacheId = Optional.empty();
                            if (messageContext().metadata().asCloudEvent().subject().isPresent()) {
                                cacheId = Optional.of(messageContext().metadata().asCloudEvent().subject().get());
                            }
                            log.debug("Consumer received CacheEvent.CacheSet for Org {} Id {} increased by {}", increaseBytesUsed.org().get(), cacheId, increaseBytesUsed.totalBytes());
                        }
                        componentClient.forKeyValueEntity(increaseBytesUsed.org().get())
                                .method(OrgEntity::increment)
                                .invokeAsync(increaseBytesUsed.totalBytes());
                        yield effects().done();
                    } else {
                        yield effects().ignore();
                    }
                }
                case CacheEvent.ChunkAdded ignored -> effects().ignore();
                case CacheEvent.CacheDeleted decreaseBytesUsed -> {
                    if (decreaseBytesUsed.org().isPresent()) {
                        if (log.isDebugEnabled()) {
                            Optional<String> cacheId = Optional.empty();
                            if (messageContext().metadata().asCloudEvent().subject().isPresent()) {
                                cacheId = Optional.of(messageContext().metadata().asCloudEvent().subject().get());
                            }
                            log.debug("Consumer received CacheEvent.CacheDeleted for Org {} Id {} decrease by {}", decreaseBytesUsed.org().get(), cacheId, decreaseBytesUsed.totalBytes());
                        }
                        componentClient.forKeyValueEntity(decreaseBytesUsed.org().get())
                                .method(OrgEntity::decrement)
                                .invokeAsync(decreaseBytesUsed.totalBytes());
                        yield effects().done();
                    } else {
                        yield effects().ignore();
                    }
                }
            };
        }
        else {
            return effects().ignore();
        }
    }
}
