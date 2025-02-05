package io.akka.cache.api;

/**
 * Constants used across the cache endpoint APIs.
 * This class cannot be instantiated.
 */
public final class EndpointConstants {
    
    // Default values
    public static final int DEFAULT_TTL = 0;
    public static final String EXCEEDED_CACHED_ALLOTMENT = "You've exceeded maximum allow bytes cached for your account level.";
    public static final String ORG = "org";
    public static final String ORG_NULL_MSG = " not found in the JWT Token";
    public static final String SERVICE_LEVEL = "serviceLevel";
    public static final String SERVICE_LEVEL_FREE = "free";
    public static final String SERVICE_LEVEL_GATLING = "gatling";
    public static final String SERVICE_LEVEL_JWT_ERR_MSG = "Invalid Service Level (%s) found in the JWT Token";
    
    // Prevent instantiation
    private EndpointConstants() {
        throw new AssertionError("Utility class - do not instantiate");
    }
}
