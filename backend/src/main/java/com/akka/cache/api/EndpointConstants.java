package com.akka.cache.api;

/**
 * Constants used across the cache endpoint APIs.
 * This class cannot be instantiated.
 */
public final class EndpointConstants {
    
    // Default values
    public static final int DEFAULT_TTL = 0;
    public static final String EXCEEDED_CACHED_ALLOTMENT = "You've exceeded maximum allow bytes cached for your account level.";
    // public static final String FREE = "FREE";
    public static final String ORG = "org";
    public static final String ORG_NULL_MSG = " not found in the JWT Token";
    public static final String SERVICE_LEVEL = "serviceLevel";
    public static final String SERVICE_LEVEL_FREE = "free";
    public static final String SERVICE_LEVEL_GATLING = "gatling";
    public static final String SERVICE_LEVEL_JWT_ERR_MSG = "Invalid Service Level (%s) found in the JWT Token";
    
    // Path segments
    public static final String BATCH_PATH = "/batch";
    public static final String CACHE_PATH = "/cache";
    public static final String CACHE_NAME_PATH = "/cacheName";
    public static final String CACHE_NAME_REPLACE_PATH = "/{cacheName}";
    public static final String ENTRIES_PATH = "/entries";
    public static final String FLUSH_PATH = "/flush";
    public static final String GET_PATH = "/get";
    public static final String KEY_REPLACE_PATH = "/{key}";
    public static final String KEYS_PATH = "/keys";
    public static final String SET_PATH = "/set";
    public static final String TTL_REPLACE_PATH = "/{ttlSeconds}";
   
    // Prevent instantiation
    private EndpointConstants() {
        throw new AssertionError("Utility class - do not instantiate");
    }
}
