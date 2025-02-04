package io.akka.cache.application;

import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import io.akka.cache.domain.CacheEvent;
import io.akka.cache.domain.PayloadChunk;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CacheViewTest extends TestKitSupport {

    private final static String CACHENAME1 = "cache1";

    private final static String KEY1 = "key1";
    private final static String KEY2 = "key2";
    private final static String KEY3 = "key3";

    private final static String PAYLOAD1 = "This is payload one!";
    private final static String PAYLOAD2 = "This is payload two!";
    private final static String PAYLOAD3 = "This is payload three!";

    @Override
    protected TestKit.Settings testKitSettings() {
        return TestKit.Settings.DEFAULT
                .withEventSourcedEntityIncomingMessages("cache");
    }

    @Test
    public void shouldGetCachedKeysSummaryForCache1() {
        EventingTestKit.IncomingMessages cacheEvents = testKit.getEventSourcedEntityIncomingMessages("cache");

        var evt1 = new CacheEvent.CacheSet(Optional.empty(), CACHENAME1, KEY1, Optional.empty(), PAYLOAD1.length(), new PayloadChunk(0, PAYLOAD1.getBytes()));
        var evt2 = new CacheEvent.CacheSet(Optional.empty(), CACHENAME1, KEY2, Optional.empty(), PAYLOAD2.length(), new PayloadChunk(0, PAYLOAD2.getBytes()));
        var evt3 = new CacheEvent.CacheSet(Optional.empty(), CACHENAME1, KEY3, Optional.empty(), PAYLOAD3.length(), new PayloadChunk(0, PAYLOAD3.getBytes()));

        cacheEvents.publish(evt1, "1");
        cacheEvents.publish(evt2, "2");
        cacheEvents.publish(evt3, "3");

        Awaitility.await()
                .ignoreExceptions()
                .atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                            CacheView.CacheSummaries summariesResponse =
                                    await(
                                            componentClient.forView()
                                                    .method(CacheView::getCacheSummaries)
                                                    .invokeAsync(CACHENAME1)
                                    );

                            CacheView.CacheSummary sumKey1 = new CacheView.CacheSummary(CACHENAME1, KEY1);
                            CacheView.CacheSummary sumKey2 = new CacheView.CacheSummary(CACHENAME1, KEY2);
                            CacheView.CacheSummary sumKey3 = new CacheView.CacheSummary(CACHENAME1, KEY3);

                            assertThat(summariesResponse.cached()).containsOnly(sumKey1, sumKey2, sumKey3);
                        }
                );
    }

    @Test
    public void shouldGetCachedKeysOnlyForCache1() {
        EventingTestKit.IncomingMessages cacheEvents = testKit.getEventSourcedEntityIncomingMessages("cache");

        var evt1 = new CacheEvent.CacheSet(Optional.empty(), CACHENAME1, KEY1, Optional.empty(), PAYLOAD1.length(), new PayloadChunk(0, PAYLOAD1.getBytes()));
        var evt2 = new CacheEvent.CacheSet(Optional.empty(), CACHENAME1, KEY2, Optional.empty(), PAYLOAD2.length(), new PayloadChunk(0, PAYLOAD2.getBytes()));
        var evt3 = new CacheEvent.CacheSet(Optional.empty(), CACHENAME1, KEY3, Optional.empty(), PAYLOAD3.length(), new PayloadChunk(0, PAYLOAD3.getBytes()));

        cacheEvents.publish(evt1, "1");
        cacheEvents.publish(evt2, "2");
        cacheEvents.publish(evt3, "3");

        Awaitility.await()
                .ignoreExceptions()
                .atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                            CacheView.CachedKeys keysResponse =
                                    await(
                                            componentClient.forView()
                                                    .method(CacheView::getCacheKeys)
                                                    .invokeAsync(CACHENAME1)
                                    );

                            assertThat(keysResponse.keys()).containsOnly(KEY1, KEY2, KEY3);
                        }
                );
    }
}