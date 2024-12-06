package com.akka.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import akka.javasdk.testkit.TestKitSupport;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import akka.http.javadsl.model.StatusCodes;
import com.akka.cache.domain.CacheAPI.*;
import com.akka.cache.application.CacheEntity;
import com.akka.cache.domain.CacheInternalGetResponse;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheJWTEndpointTest extends TestKitSupport {
    private static final Logger log = LoggerFactory.getLogger(CacheJWTEndpointTest.class);

    private static final String ORG = "ttorg";
    private static final String CACHE_NAME = "cache1";

    private static final String TEST_DESC_1 = "This is our first test";

    private static final String PAYLOAD1 = "This is Akka 3's time.";
    private static final String PAYLOAD2 = "Akka 3 is on it's way";

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
    public void httpCreateCacheName() {
        CacheNameRequest createRequest = new CacheNameRequest(CACHE_NAME, TEST_DESC_1);

        var response = await(
                httpClient.POST("/cacheName")
                        .addHeader("Authorization","Bearer "+ bearerToken)
                        .withRequestBody(createRequest)
                        .invokeAsync()
        );

        Assertions.assertEquals(StatusCodes.CREATED, response.status());
    }

    @Test
    @Order(2)
    public void httpCreateCache1Key1() {
        CacheRequest setRequest = new CacheRequest(CACHE_NAME, "key1", Optional.empty(), PAYLOAD1.getBytes());

        var response = await(
                httpClient.POST("/set")
                        .addHeader("Authorization","Bearer "+ bearerToken)
                        .withRequestBody(setRequest)
                        .invokeAsync()
        );
        Assertions.assertEquals(StatusCodes.CREATED, response.status());
        CacheInternalGetResponse cached = getCache(ORG.concat(CACHE_NAME), "key1");
        String returnedPayload = new String(cached.firstChunk().payload(), StandardCharsets.UTF_8);
        Assertions.assertEquals(PAYLOAD1, returnedPayload);
    }

    @Test
    @Order(3)
    public void httpCreateCache1Key2() {
        CacheRequest setRequest = new CacheRequest(CACHE_NAME, "key2", Optional.empty(), PAYLOAD2.getBytes());

        var response = await(
                httpClient.POST("/set")
                        .addHeader("Authorization","Bearer "+ bearerToken)
                        .withRequestBody(setRequest)
                        .invokeAsync()
        );
        Assertions.assertEquals(StatusCodes.CREATED, response.status());
        CacheInternalGetResponse cached = getCache(ORG.concat(CACHE_NAME), "key2");
        String returnedPayload = new String(cached.firstChunk().payload(), StandardCharsets.UTF_8);
        Assertions.assertEquals(PAYLOAD2, returnedPayload);
    }

    @Test
    @Order(4)
    public void httpVerifyCacheKeysView() {
        Awaitility.await()
                .ignoreExceptions()
                .atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var response = await(
                            httpClient.GET("/" + CACHE_NAME + "/keys")
                                    .addHeader("Authorization","Bearer "+ bearerToken)
                                    .responseBodyAs(CacheGetKeysResponse.class)
                                    .invokeAsync()

                    );
                    Assertions.assertEquals(StatusCodes.OK, response.status());
                    Assertions.assertEquals(2, response.body().keys().size());
                    log.info("response: {}", response.body().keys());
                });

    }
}


