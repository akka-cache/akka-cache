package com.akka.cache;

import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.api.CacheEndpoint;
import com.akka.cache.application.CacheEntity;
import com.akka.cache.application.CacheView;
import com.akka.cache.domain.Cache;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is a skeleton for implementing integration tests for an Akka application built with the Akka SDK for Java.
 *
 * It interacts with the components of the application using a componentClient
 * (already configured and provided automatically through injection).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheIntegrationTest extends TestKitSupport {
  private static final Logger log = LoggerFactory.getLogger(CacheIntegrationTest.class);

  private static final String TEST_DESC_1 = "This is our first test";

  private static final String PAYLOAD1 = "This is Akka 3's time.";
  private static final String PAYLOAD2 = "Akka 3 is on it's way";

  private Cache getCache(String cacheName, String key) {
    return await(
            componentClient
              .forKeyValueEntity(String.format("%s%s", cacheName, key))
              .method(CacheEntity::get)
              .invokeAsync()
    );
  }

  @Test
  @Order(1)
  public void httpCreateCacheName() {
    CacheEndpoint.CacheNameRequest createRequest = new CacheEndpoint.CacheNameRequest("cache1", TEST_DESC_1);

    var response = await(
            httpClient.POST("/cache/cacheName/create")
                    .withRequestBody(createRequest)
                    .invokeAsync()
    );

    Assertions.assertEquals(StatusCodes.CREATED, response.status());
  }

  @Test
  @Order(2)
  public void httpCreateCach1Key1() {
    CacheEndpoint.CacheRequest setRequest = new CacheEndpoint.CacheRequest("cache1", "key1", PAYLOAD1.getBytes());

    var response = await(
            httpClient.POST("/cache")
                    .withRequestBody(setRequest)
                    .invokeAsync()
    );
    Assertions.assertEquals(StatusCodes.CREATED, response.status());
    Cache cached = getCache("cache1", "key1");
    Assertions.assertEquals(PAYLOAD1, new String(cached.value(), StandardCharsets.UTF_8));
  }

  @Test
  @Order(3)
  public void httpCreateCach1Key2() {
    CacheEndpoint.CacheRequest setRequest = new CacheEndpoint.CacheRequest("cache1", "key2", PAYLOAD2.getBytes());

    var response = await(
            httpClient.POST("/cache")
                    .withRequestBody(setRequest)
                    .invokeAsync()
    );
    Assertions.assertEquals(StatusCodes.CREATED, response.status());
    Cache cached = getCache("cache1", "key2");
    Assertions.assertEquals(PAYLOAD2, new String(cached.value(), StandardCharsets.UTF_8));
  }

  @Test
  @Order(4)
  public void httpVerifyCachKeysView() {
    var response = await(
            httpClient.GET("/cache/cacheName/keys/cache1")
                    .invokeAsync()
    );
    Assertions.assertEquals(StatusCodes.OK, response.status());
    log.info("response: {}", response.body());
  }

}