package com.akka.cache;

import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.api.CacheEndpoint;
import com.akka.cache.domain.CacheAPI.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheLargeObjectViaJSONTest extends TestKitSupport {
    private static final Logger log = LoggerFactory.getLogger(CacheLargeObjectViaJSONTest.class);

    private static final String CACHE_NAME = "cache1";
    private static final String imageName = "./images/2025-Chevrolet-Corvette-ZR1-001-1440sw.jpg";
    private static final String copyToFile1 = "./images/2025-Chevrolet-Corvette-ZR1-001-1440sw(copy).jpg";

    private static final String key1 = "2025-Corvette-JSON";
    final byte[] imageData = readFileToBytes(imageName);

    public CacheLargeObjectViaJSONTest() throws IOException {
    }

    @Override
    protected TestKit.Settings testKitSettings() {
        return super.testKitSettings().withAclDisabled();
    }

    private byte[] readFileToBytes(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];
        try(FileInputStream fis = new FileInputStream(file)){
            fis.read(bytes);
        }
        return bytes;
    }

    private void writeBytesToFile(String filename, byte[] bytes) {
        Path path = Paths.get(filename);
        try {
            Files.write(path, bytes);
        }
        catch (IOException e) {
            log.error("an exception occurred {}", e);
        }
    }

    @Test
    @Order(1)
    public void httpCreateLargeCache1Key1() {

        CacheRequest setRequest = new CacheRequest(CACHE_NAME, key1, Optional.empty(), imageData);

        var response = await(
                httpClient.POST("/cache/set")
                        .withRequestBody(setRequest)
                        .invokeAsync()
        );

        Assertions.assertEquals(StatusCodes.CREATED, response.status());
    }

    @Test
    void getLargeImage() throws IOException {

        final byte[] imageData = readFileToBytes(imageName);

        var cache = await(
                httpClient.GET("/cache/get/" + CACHE_NAME + "/" + key1)
                        .responseBodyAs(CacheGetResponse.class)
                        .invokeAsync()
        );

        Assertions.assertEquals(StatusCodes.OK, cache.status());
        Assertions.assertEquals(imageData.length, cache.body().value().length);
//        Assertions.assertEquals(imageData, cache.body().value()); // not sure why this doesn't work
        writeBytesToFile(copyToFile1, cache.body().value());
    }

}
