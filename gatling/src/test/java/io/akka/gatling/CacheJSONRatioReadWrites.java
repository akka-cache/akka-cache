package io.akka.gatling;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.gatling.javaapi.core.Body;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CacheJSONRatioReadWrites extends Simulation {
    private Config config = ConfigFactory.load();

    private String baseUrl = config.getString("loadtest.baseUrl");

    private Base64.Encoder encoder = Base64.getEncoder();

    private FeederBuilder.Batchable<String> namesFeeder = csv("lastnames.csv")
            .circular();
    private FeederBuilder<Object> phraseFeeder = csv("phrases.csv")
            .random()
            .transform((key, value) -> {
                var encoded = encoder.encodeToString(
                        value.getBytes(StandardCharsets.UTF_8) );
                return encoded;
            });

    private Random random = new Random();

//    private var Base64.getEncoder().encode("Test".getBytes());

    Body.WithString newCacheValue = StringBody("{\"cacheName\":\"cache1\", \"key\":\"#{name}\", \"value\":\"#{phrase}\"}");

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(baseUrl)
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json");

    ScenarioBuilder scnWarmUpWrites = scenario("CacheSetWarmupScenario")
            .feed(namesFeeder)
            .feed(phraseFeeder)

            .exec(
                    http("warmup-set-cache")
                            .post("/cache/set")
                            .body(newCacheValue).asJson()
                            .check(status().is(201))
            );

    ScenarioBuilder scnWrites = scenario("CacheSetScenario")
            .feed(namesFeeder)
            .feed(phraseFeeder)

            .exec(
                    http("set-cache")
                            .post("/cache/set")
                            .body(newCacheValue).asJson()
                            .check(status().is(201))
            );

    ScenarioBuilder scnReads = scenario("CacheGetScenario")
            .feed(namesFeeder)

            .exec(
                    http("get-cache")
                            .get("/cache/cache1/#{name}/")
                            .check(status().is(200))
            );
    {
        setUp(
//                scnWarmUpWrites.injectOpen(atOnceUsers(1)).protocols(httpProtocol)
                scnWarmUpWrites.injectOpen(constantUsersPerSec(17).during(Duration.ofMinutes(1))).protocols(httpProtocol),
                scnWrites.injectOpen(
                        nothingFor(Duration.ofSeconds(75)), constantUsersPerSec(5).during(Duration.ofMinutes(10))).protocols(httpProtocol),
                scnReads.injectOpen(
                        nothingFor(Duration.ofSeconds(75)), constantUsersPerSec(95).during(Duration.ofMinutes(10))).protocols(httpProtocol)
        );
    }
}
