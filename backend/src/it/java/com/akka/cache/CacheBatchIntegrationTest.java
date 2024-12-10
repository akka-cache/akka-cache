package com.akka.cache;

import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.domain.CacheAPI.*;

import com.akka.cache.application.CacheEntity;
import com.akka.cache.domain.CacheInternalGetResponse;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheBatchIntegrationTest extends TestKitSupport {
    private static final Logger log = LoggerFactory.getLogger(CacheBatchIntegrationTest.class);

    private static final String CACHE_BASE_NAME = "cache";
    private static final String KEY = "key";
    private static final String PAYLOAD = "This is Akka 3's time:";

    @Override
    protected TestKit.Settings testKitSettings() {
        return super.testKitSettings().withAclDisabled();
    }

    private CacheInternalGetResponse getCache(String cacheName, String key) {
        return await(
                componentClient
                        .forEventSourcedEntity(cacheName.concat(key))
                        .method(CacheEntity::get)
                        .invokeAsync()
        );
    }

    @Test
    @Order(1)
    public void testCreateBatchedCache() {

        List<CacheRequest> cacheRequestList = new ArrayList();
        for (int i = 1; i < 100; i++) {
            cacheRequestList.add(new CacheRequest(CACHE_BASE_NAME + i, KEY + i, Optional.empty(), (PAYLOAD + i).getBytes()));
        }

        BatchCacheRequest batchCacheRequest = new BatchCacheRequest(cacheRequestList);

        var batchResponse = await(
                httpClient.POST("/cache/batch")
                        .responseBodyAs(BatchCacheResponse.class)
                        .withRequestBody(batchCacheRequest)
                        .invokeAsync()
        );

        Assertions.assertTrue(batchResponse.body().complete());

        for (int i = 1; i < 100; i++) {
            String cacheName = CACHE_BASE_NAME + i;
            String key = KEY + i;
            CacheInternalGetResponse cached = getCache(cacheName, key);
            String returnedPayload = new String(cached.firstChunk().payload(), StandardCharsets.UTF_8);
//            log.info("returned payload for cachName:{}, key:{} is {}", cacheName, key, returnedPayload);
            Assertions.assertEquals((PAYLOAD + i), returnedPayload);
        }
    }

    @Test
    @Order(2)
    public void testGetCacheBatched() {

        List<BatchGetCacheRequest> getBatchCacheRequest = new ArrayList();
        for (int i = 1; i < 100; i++) {
            getBatchCacheRequest.add(new BatchGetCacheRequest(CACHE_BASE_NAME + i, KEY + i));
        }

        BatchGetCacheRequests requests = new BatchGetCacheRequests(getBatchCacheRequest);

        var batchResponse = await(
                httpClient.POST("/cache/batch/get")
                        .responseBodyAs(BatchGetCacheResponse.class)
                        .withRequestBody(requests)
                        .invokeAsync()
        );

        Assertions.assertTrue(batchResponse.body().complete());

        for (int i = 1; i < 100; i++) {
            String cacheName = CACHE_BASE_NAME + i;
            String key = KEY + i;

            CacheGetResponse cacheGetResponse = batchResponse.body().results().get(i-1);
            Assertions.assertEquals(cacheName, cacheGetResponse.cacheName());
            Assertions.assertEquals(key, cacheGetResponse.key());

            String returnedPayload = new String(cacheGetResponse.value(), StandardCharsets.UTF_8);
            log.info("returned value for cachName:{}, key:{} is {}", cacheName, key, returnedPayload);
            Assertions.assertEquals((PAYLOAD + i), returnedPayload);
        }
    }

    @Test
    @Order(3)
    public void testDeleteCacheBatched() {

        List<BatchGetCacheRequest> getBatchCacheRequest = new ArrayList();
        for (int i = 1; i < 100; i++) {
            getBatchCacheRequest.add(new BatchGetCacheRequest(CACHE_BASE_NAME + i, KEY + i));
        }

        BatchGetCacheRequests requests = new BatchGetCacheRequests(getBatchCacheRequest);

        var batchResponse = await(
                httpClient.DELETE("/cache/batch")
                        .responseBodyAs(BatchDeleteCacheResponse.class)
                        .withRequestBody(requests)
                        .invokeAsync()
        );

        Assertions.assertTrue(batchResponse.body().success());

    }

}