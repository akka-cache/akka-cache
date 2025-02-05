package io.akka.cache.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CacheAPITest {

    @Test
    void testBatchCacheRequest() {
        // Test with null list
        CacheAPI.BatchCacheRequest nullRequest = new CacheAPI.BatchCacheRequest(null);
        assertTrue(nullRequest.cacheRequests().isEmpty());

        // Test with valid list
        CacheAPI.CacheRequest request = new CacheAPI.CacheRequest("test-cache", "key1", new byte[]{1, 2, 3});
        CacheAPI.BatchCacheRequest validRequest = new CacheAPI.BatchCacheRequest(List.of(request));
        assertEquals(1, validRequest.cacheRequests().size());
        assertEquals(request, validRequest.cacheRequests().get(0));

        // Test with list containing null
        List<CacheAPI.CacheRequest> listWithNull = Arrays.asList(request, null);
        CacheAPI.BatchCacheRequest requestWithNull = new CacheAPI.BatchCacheRequest(listWithNull);
        assertTrue(requestWithNull.cacheRequests().isEmpty());
    }

    @Test
    void testBatchCacheResponse() {
        CacheAPI.BatchCacheResult result = new CacheAPI.BatchCacheResult("test-cache", "key1", true);
        
        // Test with valid inputs
        CacheAPI.BatchCacheResponse validResponse = new CacheAPI.BatchCacheResponse(true, List.of(result));
        assertTrue(validResponse.complete());
        assertEquals(1, validResponse.results().size());
        assertEquals(result, validResponse.results().get(0));

        // Test with null inputs
        CacheAPI.BatchCacheResponse nullResponse = new CacheAPI.BatchCacheResponse(null, null);
        assertFalse(nullResponse.complete());
        assertTrue(nullResponse.results().isEmpty());
    }

    @Test
    void testCacheRequest() {
        byte[] testData = {1, 2, 3};
        
        // Test full constructor
        CacheAPI.CacheRequest request1 = new CacheAPI.CacheRequest(
            Optional.of("org1"), 
            "cache1", 
            "key1", 
            Optional.of(60), 
            testData
        );
        assertEquals("org1", request1.org().get());
        assertEquals("cache1", request1.cacheName());
        assertEquals("key1", request1.key());
        assertEquals(60, request1.ttlSeconds().get());
        assertArrayEquals(testData, request1.value());

        // Test null handling
        CacheAPI.CacheRequest request2 = new CacheAPI.CacheRequest(null, null, null, null, null);
        assertTrue(request2.org().isEmpty());
        assertEquals("", request2.cacheName());
        assertEquals("", request2.key());
        assertTrue(request2.ttlSeconds().isEmpty());
        assertEquals(0, request2.value().length);

        // Test convenience constructor
        CacheAPI.CacheRequest request3 = new CacheAPI.CacheRequest("cache2", "key2", testData);
        assertTrue(request3.org().isEmpty());
        assertEquals("cache2", request3.cacheName());
        assertEquals("key2", request3.key());
        assertTrue(request3.ttlSeconds().isEmpty());
        assertArrayEquals(testData, request3.value());
    }

    @Test
    void testCacheGetResponse() {
        byte[] testData = {1, 2, 3};
        
        // Test with valid inputs
        CacheAPI.CacheGetResponse validResponse = new CacheAPI.CacheGetResponse("test-cache", "key1", true, testData);
        assertEquals("test-cache", validResponse.cacheName());
        assertEquals("key1", validResponse.key());
        assertTrue(validResponse.success());
        assertArrayEquals(testData, validResponse.value());

        // Test with null inputs
        CacheAPI.CacheGetResponse nullResponse = new CacheAPI.CacheGetResponse(null, null, null, null);
        assertEquals("", nullResponse.cacheName());
        assertEquals("", nullResponse.key());
        assertFalse(nullResponse.success());
        assertEquals(0, nullResponse.value().length);

        // Verify value immutability
        byte[] mutableData = {1, 2, 3};
        CacheAPI.CacheGetResponse response = new CacheAPI.CacheGetResponse("test-cache", "key1", true, mutableData);
        mutableData[0] = 4;  // Modify original array
        assertNotEquals(mutableData[0], response.value()[0]);  // Verify response value wasn't affected
    }

    @Test
    void testBatchDeleteCacheResponse() {
        CacheAPI.CacheDeleteResponse deleteResponse = new CacheAPI.CacheDeleteResponse("test-cache", "key1", true);
        
        // Test with valid inputs
        CacheAPI.BatchDeleteCacheResponse validResponse = new CacheAPI.BatchDeleteCacheResponse(true, List.of(deleteResponse));
        assertTrue(validResponse.success());
        assertEquals(1, validResponse.cacheDeleteResponses().size());
        assertEquals(deleteResponse, validResponse.cacheDeleteResponses().get(0));

        // Test with null inputs
        CacheAPI.BatchDeleteCacheResponse nullResponse = new CacheAPI.BatchDeleteCacheResponse(null, null);
        assertFalse(nullResponse.success());
        assertTrue(nullResponse.cacheDeleteResponses().isEmpty());
    }

    @Test
    void testBatchGetCacheRequest() {
        // Test with valid inputs
        CacheAPI.BatchGetCacheRequest validRequest = new CacheAPI.BatchGetCacheRequest("test-cache", "key1");
        assertEquals("test-cache", validRequest.cacheName());
        assertEquals("key1", validRequest.key());

        // Test with null inputs
        CacheAPI.BatchGetCacheRequest nullRequest = new CacheAPI.BatchGetCacheRequest(null, null);
        assertEquals("", nullRequest.cacheName());
        assertEquals("", nullRequest.key());
    }

    @Test
    void testCacheNameRequest() {
        // Test with valid inputs
        CacheAPI.CacheNameRequest validRequest = new CacheAPI.CacheNameRequest("test-cache", "Test cache description");
        assertEquals("test-cache", validRequest.cacheName());
        assertEquals("Test cache description", validRequest.description());

        // Test with null inputs
        CacheAPI.CacheNameRequest nullRequest = new CacheAPI.CacheNameRequest(null, null);
        assertEquals("", nullRequest.cacheName());
        assertEquals("", nullRequest.description());
    }
}
