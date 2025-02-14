package io.akka.cache.application;

import akka.Done;
import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.http.HttpResponses;
import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import com.typesafe.config.ConfigFactory;
import io.akka.cache.domain.*;
import io.akka.cache.domain.CacheAPI.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
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

    protected TestKit.Settings testKitSettings() {
        var overrideSettings = ConfigFactory.parseMap(
                Map.of(
                        "app.cache-name-delete-block-size", 7
                )
        );
//        return TestKit.Settings.DEFAULT.withAdditionalConfig(overrideSettings);
        return super.testKitSettings().withAclDisabled().withAdditionalConfig(overrideSettings);
    }

    private CompletionStage<CacheView.CachedKeys> getView(String cacheName) {
        return componentClient.forView()
                .method(CacheView::getCacheKeys)
                .invokeAsync(cacheName);
    }

    private void setCache(String cacheName, String key, Cache cache) {
        String cacheId = cacheName.concat(key);
        await(componentClient.forEventSourcedEntity(cacheId)
                .method(CacheEntity::set)
                .invokeAsync(cache)
        );
    }

    @Test
    public void shouldCreateThenDeleteCachedEntities() throws ExecutionException, InterruptedException {

        log.info("shouldCreateThenDeleteCached creating caches...");
        for (int i = 1; i <= NUMBER_OF_CACHES; i++) {
            Optional<Duration> evtTTL = Optional.empty();
            byte[] payload = ("my payload " + i).getBytes();
            List<PayloadChunk> chunks = new ArrayList<>(List.of(new PayloadChunk(0, payload)));
            String key = "key" + i;
            Cache toCache = new Cache(Optional.empty(), CACHENAME1, key, evtTTL, false, payload.length, false, chunks);
            setCache(CACHENAME1, "key" + i, toCache);
        }

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

        log.info("shouldCreateThenFlushCachedEntities creating cacheName {}", CACHENAME2);
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

        for (int i = 1; i <= NUMBER_OF_CACHES; i++) {
            Optional<Duration> evtTTL = random.nextBoolean() ? Optional.of(this.ttl) : Optional.empty();
            byte[] payload = ("my payload " + i).getBytes();
            List<PayloadChunk> chunks = new ArrayList<>(List.of(new PayloadChunk(0, payload)));
            String key = "key" + i;
            Cache toCache = new Cache(Optional.empty(), CACHENAME2, key, evtTTL, false, payload.length, false, chunks);
            setCache(CACHENAME2, "key" + i, toCache);
        }

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
