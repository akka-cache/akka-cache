package com.akka.cache;

import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.application.CacheEntity;
import com.akka.cache.application.OrgEntity;
import com.akka.cache.domain.CacheAPI.*;
import com.akka.cache.domain.CacheInternalGetResponse;
import com.akka.cache.domain.Organization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.fieldIn;
import static org.hamcrest.Matchers.equalTo;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheJWTBatchIntegrationTest extends TestKitSupport {
    private static final Logger log = LoggerFactory.getLogger(CacheJWTBatchIntegrationTest.class);

    private static final String ORG = "ttorg";
    private static final String CACHE_BASE_NAME = "cache";
    private static final String KEY = "key";
    private static final String PAYLOAD = "This is Akka 3's time:";
    private static final Duration ONE_HUNDERED_MILLISECONDS = Duration.ofMillis(100);

    String bearerToken = bearerTokenWith(
            Map.of("iss", "gcp", "org", ORG, "serviceLevel", "FREE")
    );

    @Override
    protected TestKit.Settings testKitSettings() {
        var appFreeServiceLevelMaxBytes = ConfigFactory.parseMap(Map.of(
                "app.free-service-level-max-bytes", "2000"));
        return TestKit.Settings.DEFAULT.withAdditionalConfig(appFreeServiceLevelMaxBytes);
    }

    private CacheInternalGetResponse getCache(String cacheName, String key) {
        return await(
                componentClient
                        .forEventSourcedEntity(cacheName.concat(key))
                        .method(CacheEntity::get)
                        .invokeAsync()
        );
    }

    private Organization getOrg(String org) {
        return await(
                componentClient
                        .forKeyValueEntity(org)
                        .method(OrgEntity::get)
                        .invokeAsync()
        );
    }

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

        List<CacheRequest> cacheRequestList = new ArrayList<>();
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

        List<BatchGetCacheRequest> getBatchCacheRequest = new ArrayList<>();
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
    }

    @Test
    @Order(3)
    public void testOrgByteCounts() throws InterruptedException {
        Awaitility.await()
                .ignoreExceptions()
                .atMost(5, TimeUnit.SECONDS)
                .with().pollInterval(ONE_HUNDERED_MILLISECONDS).and().with().pollDelay(20, MILLISECONDS)
                .until(() -> getOrg(ORG).cacheCount(), equalTo(99));
    }

    @Test
    @Order(4)
    public void testMaxBytesLimitByOrg() {
        CacheRequest setRequest = new CacheRequest(CACHE_BASE_NAME.concat("test"), "testerkey2", Optional.empty(), PAYLOAD.getBytes());

        var response = await(
                httpClient.POST("/set")
                        .addHeader("Authorization","Bearer "+ bearerToken)
                        .withRequestBody(setRequest)
                        .invokeAsync()
        );
        Assertions.assertEquals(StatusCodes.BAD_REQUEST, response.status());
    }

    @Test
    @Order(5)
    public void testDeleteCacheBatched() {

        List<BatchGetCacheRequest> getBatchCacheRequest = new ArrayList<>();
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

    @Test
    @Order(6)
    public void testOrgTotalCounts() throws InterruptedException {
        Awaitility.await()
                .ignoreExceptions()
                .atMost(5, TimeUnit.SECONDS)
                .with().pollInterval(ONE_HUNDERED_MILLISECONDS).and().with().pollDelay(20, MILLISECONDS)
                .until(() -> getOrg(ORG).cacheCount(), equalTo(0));
    }

}