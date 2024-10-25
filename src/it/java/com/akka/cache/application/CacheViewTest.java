package com.akka.cache.application;

import akka.javasdk.testkit.EventingTestKit;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.domain.Cache;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
                .withKeyValueEntityIncomingMessages("cache");
    }

    @Test
    public void shouldGetCachedKeysForCache1() {
        EventingTestKit.IncomingMessages cacheEvents = testKit.getKeyValueEntityIncomingMessages("cache");

        Cache cache1 = new Cache(CACHENAME1, KEY1, PAYLOAD1.getBytes());
        Cache cache2 = new Cache(CACHENAME1, KEY2, PAYLOAD1.getBytes());
        Cache cache3 = new Cache(CACHENAME1, KEY3, PAYLOAD1.getBytes());

        cacheEvents.publish(cache1, "1");
        cacheEvents.publish(cache2, "2");
        cacheEvents.publish(cache3, "3");

        Awaitility.await()
                .ignoreExceptions()
                .atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                            CacheView.CacheSummaries summariesResponse =
                                    await(
                                            componentClient.forView()
                                                    .method(CacheView::getCacheKeys)
                                                    .invokeAsync(CACHENAME1)
                                    );

                            CacheView.CacheSummary sumKey1 = new CacheView.CacheSummary(CACHENAME1, KEY1);
                            CacheView.CacheSummary sumKey2 = new CacheView.CacheSummary(CACHENAME1, KEY2);
                            CacheView.CacheSummary sumKey3 = new CacheView.CacheSummary(CACHENAME1, KEY3);

                            assertThat(summariesResponse.cached()).containsOnly(sumKey1, sumKey2, sumKey3);
                        }
                );
    }
}