package com.akka.cache.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import com.akka.cache.domain.CacheEvent;
import com.akka.cache.domain.PrometheusMetricsAPI.PrometheusMetric;
import com.akka.cache.injection.PrometheusMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ComponentId("cached-events-consumer")
@Consume.FromEventSourcedEntity(CacheEntity.class)
public class CacheConsumer extends Consumer {
    private static final Logger log = LoggerFactory.getLogger(CacheConsumer.class);

    private final ComponentClient componentClient;
    private final PrometheusMetrics metrics;

    public CacheConsumer(ComponentClient componentClient, PrometheusMetrics metrics) {
        this.componentClient = componentClient;
        this.metrics = metrics;
    }

    public Effect onEvent(CacheEvent event) {
        return switch (event) {
            case CacheEvent.CacheSet increaseBytesUsed -> {
                // TODO: do I need to retry in case of failure
                if (increaseBytesUsed.org().isPresent()) {
                    if (log.isDebugEnabled()) {
                        Optional<String> cacheId = Optional.empty();
                        if (messageContext().metadata().asCloudEvent().subject().isPresent()) {
                            cacheId = Optional.of(messageContext().metadata().asCloudEvent().subject().get());
                        }
                        log.debug("Consumer received CacheEvent.CacheSet for Org {} Id {} increased by {} time {}", increaseBytesUsed.org().get(), cacheId, increaseBytesUsed.totalBytes(), messageContext().metadata().asCloudEvent().time());
                    }
                    componentClient.forKeyValueEntity(increaseBytesUsed.org().get())
                            .method(OrgEntity::increment)
                            .invokeAsync(increaseBytesUsed.totalBytes());
                    yield effects().done();
                }
                else {
                    yield effects().ignore();
                }
            }
            case CacheEvent.ChunkAdded ignored -> effects().ignore();
            case CacheEvent.CacheDeleted decreaseBytesUsed -> {
                // TODO: do I need to retry in case of failure
                if (decreaseBytesUsed.org().isPresent()) {
                    if (log.isDebugEnabled()) {
                        Optional<String> cacheId = Optional.empty();
                        if (messageContext().metadata().asCloudEvent().subject().isPresent()) {
                            cacheId = Optional.of(messageContext().metadata().asCloudEvent().subject().get());
                        }
                        log.debug("Consumer received CacheEvent.CacheDeleted for Org {} Id {} decrease by {} time {}", decreaseBytesUsed.org().get(), cacheId, decreaseBytesUsed.totalBytes(), messageContext().metadata().asCloudEvent().time());
                    }
                    componentClient.forKeyValueEntity(decreaseBytesUsed.org().get())
                            .method(OrgEntity::decrement)
                            .invokeAsync(decreaseBytesUsed.totalBytes());
                    yield effects().done();
                }
                else {
                    yield effects().ignore();
                }
            }
        };
    }
}
