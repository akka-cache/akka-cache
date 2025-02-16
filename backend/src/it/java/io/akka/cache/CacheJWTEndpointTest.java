package io.akka.cache;

import akka.javasdk.testkit.TestKit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.ConfigFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import akka.javasdk.testkit.TestKitSupport;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import akka.http.javadsl.model.StatusCodes;
import io.akka.cache.domain.CacheAPI.*;
import io.akka.cache.application.CacheEntity;
import io.akka.cache.domain.CacheInternalGetResponse;
import io.akka.cache.domain.Organization;
import io.akka.cache.application.OrgEntity;

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
            Map.of("iss", "https://session.firebase.google.com/akka-cache", "org", ORG, "serviceLevel", "free")
    );

    @Override
    protected TestKit.Settings testKitSettings() {
        var overrideSettings = ConfigFactory.parseMap(
                Map.of(
                        "app.enable-org-service-level-saas", true
                )
        );
        return TestKit.Settings.DEFAULT.withAdditionalConfig(overrideSettings);
    }

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
                            httpClient.GET("/cacheName/" + CACHE_NAME + "/keys")
                                    .addHeader("Authorization","Bearer "+ bearerToken)
                                    .responseBodyAs(CacheGetKeysResponse.class)
                                    .invokeAsync()

                    );
                    Assertions.assertEquals(StatusCodes.OK, response.status());
                    Assertions.assertEquals(2, response.body().keys().size());
                    log.info("response: {}", response.body().keys());
                });

    }

    private Organization getOrg(String org) {
        return await(
                componentClient
                        .forKeyValueEntity(org)
                        .method(OrgEntity::get)
                        .invokeAsync()
        );
    }

    @Test
    @Order(5)
    public void verifyOrgBytesUsed() {
        // this test relies on the Organization and the CacheConsumer
        var org = getOrg(ORG);

        long payloadsSize = PAYLOAD1.length() + PAYLOAD2.length();

        Assertions.assertEquals(2, org.cacheCount());
        Assertions.assertEquals(payloadsSize, org.totalBytesCached());

    }
}


