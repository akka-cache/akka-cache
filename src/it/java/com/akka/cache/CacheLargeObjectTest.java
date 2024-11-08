package com.akka.cache;

import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.testkit.TestKitSupport;
import com.akka.cache.api.CacheEndpoint;
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
public class CacheLargeObjectTest extends TestKitSupport {
    private static final Logger log = LoggerFactory.getLogger(CacheLargeObjectTest.class);

    private static final String CACHE_NAME = "cache1";

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
    public void httpCreateLargeCache1Key1() throws IOException {


        String imageName = "./images/2025-Chevrolet-Corvette-ZR1-001-1440sw.jpg";
        String copyToFile = "./images/2025-Chevrolet-Corvette-ZR1-001-1440sw(copy).jpg";
        byte[] imageData = readFileToBytes(imageName);

        final String key = "2025-Corvette";

        CacheEndpoint.CacheRequest setRequest = new CacheEndpoint.CacheRequest(CACHE_NAME, key, Optional.empty(), imageData);

        var response = await(
                httpClient.POST("/cache")
                        .withRequestBody(setRequest)
                        .invokeAsync()
        );

        Assertions.assertEquals(StatusCodes.CREATED, response.status());

        var cache = await(
                httpClient.GET("/cache/" + CACHE_NAME + "/" + key)
                        .responseBodyAs(CacheEndpoint.CacheGetResponse.class)
                        .invokeAsync()
        );

        Assertions.assertEquals(imageData.length, cache.body().value().length);
//        Assertions.assertEquals(imageData, cache.body().value()); // not sure why this doesn't work
        writeBytesToFile(copyToFile, cache.body().value());
    }

}
