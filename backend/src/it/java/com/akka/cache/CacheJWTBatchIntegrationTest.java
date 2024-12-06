package com.akka.cache;

import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.application.CacheEntity;
import com.akka.cache.application.OrgEntity;
import com.akka.cache.domain.CacheAPI.*;
import com.akka.cache.domain.CacheInternalGetResponse;
import com.akka.cache.domain.Organization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheJWTBatchIntegrationTest extends TestKitSupport {
    private static final Logger log = LoggerFactory.getLogger(CacheJWTBatchIntegrationTest.class);

    private static final String ORG = "ttorg";
    private static final String CACHE_BASE_NAME = "cache";
    private static final String KEY = "key";
    private static final String PAYLOAD = "This is Akka 3's time:";

    String bearerToken = bearerTokenWith(
            Map.of("iss", "gcp", "org", ORG, "serviceLevel", "FREE")
    );

    private CacheInternalGetResponse getCache(String cacheName, String key) {
        return await(
                componentClient
                        .forEventSourcedEntity(cacheName.concat(key))
                        .method(CacheEntity::get)
                        .invokeAsync()
        );
    }

/*
    private Organization getOrg(String org) {
        return await(
                componentClient
                        .forKeyValueEntity(org)
                        .method(OrgEntity::get)
                        .invokeAsync()
        );
    }
*/

    private String bearerTokenWith(Map<String, String> claims) {
        // setting algorithm to none
        try {
            String header = Base64.getEncoder().encodeToString("""
                    {
                      "alg": "none"
                    }
                    """.getBytes());
            byte[] jsonClaims = new ObjectMapper().writeValueAsBytes(claims);
            String payload = Base64.getEncoder().encodeToString(jsonClaims);

            // no validation is done for integration tests, thus no signature required
            return header + "." + payload;
        }
        catch (JsonProcessingException ex) {
            log.error("A JsonProcessingException exception occurred: {}", ex.getMessage());
            return null;
        }
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
                httpClient.POST("/batch")
                        .addHeader("Authorization","Bearer "+ bearerToken)
                        .responseBodyAs(BatchCacheResponse.class)
                        .withRequestBody(batchCacheRequest)
                        .invokeAsync()
        );

        Assertions.assertTrue(batchResponse.body().complete());

        for (int i = 1; i < 100; i++) {
            String cacheName = CACHE_BASE_NAME + i;
            String key = KEY + i;
            CacheInternalGetResponse cached = getCache(ORG.concat(cacheName), key);
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
                httpClient.POST("/batch/get")
                        .addHeader("Authorization","Bearer "+ bearerToken)
                        .responseBodyAs(BatchGetCacheResponse.class)
                        .withRequestBody(requests)
                        .invokeAsync()
        );

        Assertions.assertTrue(batchResponse.body().complete());

        long countTotalBytes = 0L;

        for (int i = 1; i < 100; i++) {
            String cacheName = CACHE_BASE_NAME + i;
            String key = KEY + i;

            CacheGetResponse cacheGetResponse = batchResponse.body().results().get(i-1);
            Assertions.assertEquals(ORG.concat(cacheName), cacheGetResponse.cacheName());
            Assertions.assertEquals(key, cacheGetResponse.key());

            String returnedPayload = new String(cacheGetResponse.value(), StandardCharsets.UTF_8);
            log.info("returned value for cachName:{}, key:{} is {}", ORG.concat(cacheName), key, returnedPayload);
            countTotalBytes += returnedPayload.length();
            Assertions.assertEquals((PAYLOAD + i), returnedPayload);
        }

        log.info("Total bytes returned {}", countTotalBytes);
/* We must need to wait for a while before projection happens
        Organization org = getOrg(ORG);
        log.info("Current org total bytes {}", org.totalBytesCached());
        Assertions.assertEquals(countTotalBytes, org.totalBytesCached());
*/
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
                httpClient.DELETE("/batch")
                        .addHeader("Authorization","Bearer "+ bearerToken)
                        .responseBodyAs(BatchDeleteCacheResponse.class)
                        .withRequestBody(requests)
                        .invokeAsync()
        );

        Assertions.assertTrue(batchResponse.body().success());

    }

}