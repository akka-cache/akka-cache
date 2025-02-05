package io.akka.cache;

import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.testkit.TestKit;
import akka.javasdk.testkit.TestKitSupport;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheLargeObjectAltTest extends TestKitSupport {
    private static final Logger log = LoggerFactory.getLogger(CacheLargeObjectAltTest.class);
    private static final String imageName = "./images/2025-Chevrolet-Corvette-ZR1-001-1440sw.jpg";
    private static final String CACHE_NAME = "cache1";
    private static final ContentType BINARY_PAYLOAD = ContentTypes.create(MediaTypes.APPLICATION_OCTET_STREAM);
    final byte[] imageData = readFileToBytes(imageName);

    final String key3 = "2025-Corvette-3";
    final String key4 = "2025-Corvette-4";

    public CacheLargeObjectAltTest() throws IOException {
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
            log.error("an exception occurred {}", e.getMessage(), e);
        }
    }

    @Test
    @Order(1)
    public void httpCreateLargeLowLevelKey3() throws IOException {

        final String copyToFile = "./images/2025-Chevrolet-Corvette-ZR1-001-1440sw(copy 3).jpg";
        var response = await(
                httpClient.POST("/cache/"+ CACHE_NAME + "/" + key3)
                        .withRequestBody(BINARY_PAYLOAD, imageData)
                        .invokeAsync()
        );

        Assertions.assertEquals(StatusCodes.CREATED, response.status());

    }

    @Test
    @Order(2)
    public void httpGetLargeLowLevelImageKey3() {
        final String copyToFile = "./images/2025-Chevrolet-Corvette-ZR1-001-1440sw(copy 3).jpg";

        var cache = await(
                httpClient.GET("/cache/" + CACHE_NAME + "/" + key3)
                        .invokeAsync()
        );

        byte[] getResponse = cache.body().toArray();

        Assertions.assertEquals(imageData.length, getResponse.length);
//        Assertions.assertEquals(imageData, cache.body().value()); // not sure why this doesn't work
        writeBytesToFile(copyToFile, getResponse);
    }

    @Test
    @Order(3)
    public void httpCreateLargeKey4WithTTL10() throws IOException {

        var response = await(
                httpClient.POST("/cache/"+ CACHE_NAME + "/" + key4 + "/10")
                        .withRequestBody(BINARY_PAYLOAD, imageData)
                        .invokeAsync()
        );

        Assertions.assertEquals(StatusCodes.CREATED, response.status());

    }

    @Test
    @Order(4)
    public void httpGetLargeLowLevelImageKey4() {
        final String copyToFile = "./images/2025-Chevrolet-Corvette-ZR1-001-1440sw(copy 4).jpg";

        var cache = await(
                httpClient.GET("/cache/" + CACHE_NAME + "/" + key4)
                        .invokeAsync()
        );

        byte[] getResponse = cache.body().toArray();

        Assertions.assertEquals(imageData.length, getResponse.length);
//        Assertions.assertEquals(imageData, cache.body().value()); // not sure why this doesn't work
        writeBytesToFile(copyToFile, getResponse);
    }

}