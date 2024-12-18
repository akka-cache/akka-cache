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

import java.time.Duration;
import java.util.Base64;
import java.util.Random;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CacheLowLevelSetScenario extends Simulation {
    private static final Logger log = LoggerFactory.getLogger(CacheLowLevelSetScenario.class);

    private Config config = ConfigFactory.load();

    private String baseUrl = config.getString("loadtest.baseUrl");

    private Base64.Encoder encoder = Base64.getEncoder();

    private FeederBuilder.Batchable<String> namesFeeder = csv("lastnames.csv");
    private FeederBuilder<String> phraseFeeder = csv("phrases.csv")
            .random();

    private Random random = new Random();

//    private var Base64.getEncoder().encode("Test".getBytes());

    Body.WithString newCacheValue = StringBody("#{phrase}");

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(baseUrl)
                    .acceptHeader("application/json")
                    .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("CacheSetScenario")
            .feed(namesFeeder)
            .feed(phraseFeeder)

            .exec(
                    http("set-cache")
                            .post("/cache/cache1/#{name}")
                            .header("content-type", "application/octet-stream")
                            .body(newCacheValue)
                            .check(status().is(201))
            );
    {
        setUp(
//                myFirstScenario.injectOpen(constantUsersPerSec(2).during(60))
//    scn.injectOpen(atOnceUsers(1))
//    scn.injectOpen(rampUsers(100).during(Duration.ofMinutes(3)))
    scn.injectOpen(rampUsers(1000).during(Duration.ofMinutes(5)))
// simulation set up -> -> https://docs.gatling.io/reference/script/core/injection/#open-model
/*
            scn.injectOpen(
                    nothingFor(Duration.ofSeconds(4)), // 1
                    atOnceUsers(10), // 2
                    rampUsers(10).during(Duration.ofSeconds(5)), // 3
                    constantUsersPerSec(20).during(Duration.ofSeconds(15)), // 4
                    constantUsersPerSec(20).during(Duration.ofSeconds(15)).randomized(), // 5
                    rampUsersPerSec(10).to(20).during(Duration.ofMinutes(10)), // 6
                    rampUsersPerSec(10).to(20).during(Duration.ofMinutes(10)).randomized(), // 7
                    stressPeakUsers(1000).during(Duration.ofSeconds(20)) // 8
            )
*/
            .protocols(httpProtocol)
        );
    }
}
