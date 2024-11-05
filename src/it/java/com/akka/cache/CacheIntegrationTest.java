package com.akka.cache;

import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.http.StrictResponse;
import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.api.CacheEndpoint;
import com.akka.cache.application.CacheEntity;
import com.akka.cache.application.CacheView;
import com.akka.cache.domain.Cache;
import com.akka.cache.domain.CacheName;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This is a skeleton for implementing integration tests for an Akka application built with the Akka SDK for Java.
 *
 * It interacts with the components of the application using a componentClient
 * (already configured and provided automatically through injection).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheIntegrationTest extends TestKitSupport {
  private static final Logger log = LoggerFactory.getLogger(CacheIntegrationTest.class);

  private static final String CACHE_NAME = "cache1";
  
  private static final String TEST_DESC_1 = "This is our first test";

  private static final String PAYLOAD1 = "This is Akka 3's time.";
  private static final String PAYLOAD2 = "Akka 3 is on it's way";

  private Cache getCache(String cacheName, String key) {
    return await(
            componentClient
              .forEventSourcedEntity(cacheName.concat(key))
              .method(CacheEntity::get)
              .invokeAsync()
    );
  }

  @Test
  @Order(1)
  public void httpCreateCacheName() {
    CacheEndpoint.CacheNameRequest createRequest = new CacheEndpoint.CacheNameRequest(CACHE_NAME, TEST_DESC_1);

    var response = await(
            httpClient.POST("/cache/cacheName")
                    .withRequestBody(createRequest)
                    .invokeAsync()
    );

    Assertions.assertEquals(StatusCodes.CREATED, response.status());
  }

  @Test
  @Order(2)
  public void httpCreateCache1Key1() {
    CacheEndpoint.CacheRequest setRequest = new CacheEndpoint.CacheRequest(CACHE_NAME, "key1", Optional.empty(), PAYLOAD1.getBytes());

    var response = await(
            httpClient.POST("/cache")
                    .withRequestBody(setRequest)
                    .invokeAsync()
    );
    Assertions.assertEquals(StatusCodes.CREATED, response.status());
    Cache cached = getCache(CACHE_NAME, "key1");
    String returnedPayload = new String(cached.chunks().get(0).payload(), StandardCharsets.UTF_8);
    Assertions.assertEquals(PAYLOAD1, returnedPayload);
  }

  @Test
  @Order(3)
  public void httpCreateCache1Key2() {
    CacheEndpoint.CacheRequest setRequest = new CacheEndpoint.CacheRequest(CACHE_NAME, "key2", Optional.empty(), PAYLOAD2.getBytes());

    var response = await(
            httpClient.POST("/cache")
                    .withRequestBody(setRequest)
                    .invokeAsync()
    );
    Assertions.assertEquals(StatusCodes.CREATED, response.status());
    Cache cached = getCache(CACHE_NAME, "key2");
    String returnedPayload = new String(cached.chunks().get(0).payload(), StandardCharsets.UTF_8);
    Assertions.assertEquals(PAYLOAD2, returnedPayload);
  }

  @Test
  @Order(4)
  public void httpVerifyCachKeysView() {
    Awaitility.await()
            .ignoreExceptions()
            .atMost(20, TimeUnit.SECONDS)
            .untilAsserted(() -> {
              var response = await(
                      httpClient.GET("/cache/cacheName/" + CACHE_NAME + "/keys")
                              .responseBodyAs(CacheView.CacheSummaries.class)
                              .invokeAsync()

              );
              Assertions.assertEquals(StatusCodes.OK, response.status());
              Assertions.assertEquals(response.body().cached().size(), 2);
              // how to convert parse the response body
              log.info("response: {}", response.body());
            });
  }

}