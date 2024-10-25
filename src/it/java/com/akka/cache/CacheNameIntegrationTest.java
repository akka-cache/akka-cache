package com.akka.cache;

import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.JsonSupport;
import com.akka.cache.api.CacheEndpoint;
import com.akka.cache.application.CacheNameEntity;
import com.akka.cache.domain.CacheName;
import org.junit.jupiter.api.*;
import akka.javasdk.testkit.TestKitSupport;

import java.util.Optional;

/**
 * This is a skeleton for implementing integration tests for an Akka application built with the Akka SDK for Java.
 *
 * It interacts with the components of the application using a componentClient
 * (already configured and provided automatically through injection).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheNameIntegrationTest extends TestKitSupport {

  private static final String TEST_DESC_1 = "This is our first test";
  private static final String TEST_DESC_1_MODIFIED = "This is our first test modification";

  private CacheName getCacheName(String cacheName) {
    return await(
            componentClient
                    .forKeyValueEntity(cacheName)
                    .method(CacheNameEntity::get)
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
    Assertions.assertEquals(Optional.of(TEST_DESC_1), getCacheName("cache1").description());
  }

  @Test
  @Order(2)
  public void httpModifyCacheNameDescription() {
    CacheEndpoint.CacheNameRequest updateRequest = new CacheEndpoint.CacheNameRequest("cache1", TEST_DESC_1_MODIFIED);

    var response = await(
            httpClient.POST("/cache/cacheName/update")
                    .withRequestBody(updateRequest)
                    .invokeAsync()
    );

    Assertions.assertEquals(StatusCodes.ACCEPTED, response.status());
    Assertions.assertEquals(Optional.of(TEST_DESC_1_MODIFIED), getCacheName("cache1").description());

  }

  @Test
  @Order(3)
  public void httpGetModifiedCacheNameDescription() {

    var response = await(
            httpClient.GET("/cache/cacheName/cach1")
                    .responseBodyAs(CacheName.class)
                    .invokeAsync()
    );

    Assertions.assertEquals(StatusCodes.OK, response.status());
    // test the actual return
    Assertions.assertEquals(Optional.of(TEST_DESC_1_MODIFIED), response.body().description());
    // compare to the actual entity's return
    Assertions.assertEquals(Optional.of(TEST_DESC_1_MODIFIED), getCacheName("cache1").description());

  }
}
