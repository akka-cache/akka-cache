package com.akka.cache.application;

import akka.Done;
import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.domain.CacheAPI.*;
import com.akka.cache.domain.CacheEvent;
import com.akka.cache.domain.CacheName;
import com.akka.cache.domain.PayloadChunk;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheNameDeleteWorkflowTest extends TestKitSupport {
    private static final Logger log = LoggerFactory.getLogger(CacheNameDeleteWorkflowTest.class);

    private final static String CACHENAME1 = "cache1";
    private final static String CACHENAME2 = "cache2";
    private final static String CACHENAME_DESC = "This is a simple cache test";
    private final static int NUMBER_OF_CACHES = 22;
    private final Duration timeout = Duration.ofSeconds(6);
    private final Duration ttl = Duration.ofSeconds(30);
    private final Random random = new Random();

    @Override
    protected TestKit.Settings testKitSettings() {
        return TestKit.Settings.DEFAULT
                .withEventSourcedEntityIncomingMessages("cache");
    }

    private CompletionStage<CacheView.CachedKeys> getView(String cacheName) {
        return componentClient.forView()
                .method(CacheView::getCacheKeys)
                .invokeAsync(cacheName);
    }

    @Test
    public void shouldCreateThenDeleteCachedEntities() throws ExecutionException, InterruptedException {
        final EventingTestKit.IncomingMessages cacheEvents = testKit.getEventSourcedEntityIncomingMessages("cache");

        Awaitility.await().until(() -> {
            log.info("shouldCreateThenDeleteCached creating caches...");
            for (int i = 1; i <= NUMBER_OF_CACHES; i++) {
                Optional<Duration> evtTTL = Optional.empty();
                String payload = "my payload " + i;
                var event = new CacheEvent.CacheSet(Optional.empty(), CACHENAME1, "key" + i, evtTTL, payload.length(), new PayloadChunk(0, payload.getBytes()));
                log.info("creating {} {}", CACHENAME1, "key" + i);
                cacheEvents.publish(event, String.valueOf(i));
            }
            return true;
        });

        Awaitility.await()
            .ignoreExceptions()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> {

                // make sure we have everything populated
                log.info("shouldCreateThenDeleteCached verifying the view has been populated...");
                CacheView.CachedKeys insertedKeys = await(getView(CACHENAME1));
                Assertions.assertEquals(NUMBER_OF_CACHES, insertedKeys.keys().size());

            });

        log.info("shouldCreateThenDeleteCached starting CacheNameDeleteWorkflowTest on {} ...", CACHENAME1);
        var startDeletionSetup = new CacheNameDeleteWorkflow.StartDeletionsSetup(CACHENAME1, false);
        Done done = await(componentClient.forWorkflow(CACHENAME1)
                .method(CacheNameDeleteWorkflow::startDeletions)
                .invokeAsync(startDeletionSetup));

        log.info("shouldCreateThenDeleteCached workflow on {} has completed ...", CACHENAME1);

        Awaitility.await()
                .ignoreExceptions()
                .atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    // make sure we have everything populated
                    log.info("shouldCreateThenDeleteCached verifying the view has no entries ...");

                    var cache1View2 = getView(CACHENAME1);
                    assertThat(cache1View2)
                            .succeedsWithin(timeout)
                            .satisfies(res -> assertThat(res.keys().isEmpty()));
                });
    }

    private CacheName getCacheName(String cacheName) {
        return await(
                componentClient
                        .forEventSourcedEntity(cacheName)
                        .method(CacheNameEntity::get)
                        .invokeAsync()
        );
    }

    @Test
    public void shouldCreateThenFlushCachedEntities() throws ExecutionException, InterruptedException {
        final EventingTestKit.IncomingMessages cacheEvents = testKit.getEventSourcedEntityIncomingMessages("cache");

        Awaitility.await()
                .ignoreExceptions()
                .atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    CacheNameRequest createRequest = new CacheNameRequest(CACHENAME2, CACHENAME_DESC);
                    var createCacheNameResponse =
                            httpClient.POST("/cache/cacheName")
                                    .withRequestBody(createRequest)
                                    .invokeAsync();
                    assertThat(createCacheNameResponse)
                            .succeedsWithin(timeout)
                            .satisfies(res -> {
                                Assertions.assertEquals(StatusCodes.CREATED, res.status());
                                Assertions.assertEquals(Optional.of(CACHENAME_DESC), getCacheName(CACHENAME2).description());
                            });
                });

        Awaitility.await().until(() -> {
            log.info("shouldCreateThenDeleteCached creating caches...");
            for (int i = 1; i <= NUMBER_OF_CACHES; i++) {
                Optional<Duration> evtTTL = random.nextBoolean() ? Optional.of(this.ttl) : Optional.empty();
                String payload = "my payload " + i;
                var event = new CacheEvent.CacheSet(Optional.empty(), CACHENAME2, "key" + i, evtTTL, payload.length(), new PayloadChunk(0, payload.getBytes()));
                cacheEvents.publish(event, String.valueOf(i));
            }
            return true;
        });

        Awaitility.await()
                .ignoreExceptions()
                .atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    // make sure we have everything populated
                    log.info("shouldCreateThenDeleteCached verifying the view has been populated...");
                    CacheView.CachedKeys insertedKeys = await(getView(CACHENAME2));
                    Assertions.assertEquals(NUMBER_OF_CACHES, insertedKeys.keys().size());

                });

        log.info("shouldCreateThenDeleteCached starting CacheNameDeleteWorkflowTest on {} ...", CACHENAME2);
        var startDeletionSetup = new CacheNameDeleteWorkflow.StartDeletionsSetup(CACHENAME2, true);
        Done done = await(componentClient.forWorkflow(CACHENAME2)
                .method(CacheNameDeleteWorkflow::startDeletions)
                .invokeAsync(startDeletionSetup));

        log.info("shouldCreateThenDeleteCached on {} has completed ...", CACHENAME2);


        Awaitility.await()
                .ignoreExceptions()
                .atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    // make sure we have everything populated
                    log.info("shouldCreateThenDeleteCached verifying the view has no entries ...");

                    var cache2View2 = getView(CACHENAME2);
                    assertThat(cache2View2)
                            .succeedsWithin(timeout)
                            .satisfies(res -> {
                                assertThat(res.keys().isEmpty());
                            });
                });

        Awaitility.await().until(() -> {
                    var getCacheNameResponse =
                            await(httpClient.GET("/cache/cacheName/" + CACHENAME2)
                                    .responseBodyAs(CacheName.class)
                                    .invokeAsync());
                    assertThat(getCacheNameResponse)
                            .satisfies(res -> {
                                // make sure that we didn't delete the cacheName
                                Assertions.assertEquals(StatusCodes.OK, res.status());
                                Assertions.assertEquals(CACHENAME2, res.body().cacheName());
                                Assertions.assertEquals(Optional.of(CACHENAME_DESC), res.body().description());
                            });
                    return true;
                });

    }

}
