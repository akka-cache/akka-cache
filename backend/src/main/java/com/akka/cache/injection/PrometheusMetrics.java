package com.akka.cache.injection;

import akka.NotUsed;
import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import akka.javasdk.http.HttpClient;
import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.http.StrictResponse;
import akka.stream.BoundedSourceQueue;
import akka.stream.Materializer;
import akka.stream.QueueOfferResult;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RetryFlow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.akka.cache.domain.PrometheusMetricsAPI.PrometheusMetric;
import com.typesafe.config.Config;
import io.akka.cache.serialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Setup
public class PrometheusMetrics implements ServiceSetup {
    private static final Logger log = LoggerFactory.getLogger(PrometheusMetrics.class);

    private final String METRIC_PREFIX = "cache-";

    private final Boolean metricsEnabled;

    private HttpClient httpClient;
    private BoundedSourceQueue<PrometheusMetric> sourceQueue;

    private final Duration minBackoff;
    private final Duration maxBackoff;
    private final double randomFactor;
    private final int maxRetries;

    public PrometheusMetrics(Config config, Materializer materializer, HttpClientProvider httpClient) {
        this.metricsEnabled = config.getBoolean("app.prometheus-metrics-enabled");
        String prometheusPushGateway = config.getString("app.prometheus-push-gateway");
        int streamBufferSize = config.getInt("app.prometheus-stream-buffer-size");
        int maxSamplesPerSec = config.getInt("app.prometheus-push-max-samples-per-sec");
        this.minBackoff = Duration.ofMillis(config.getInt("app.prometheus-push-min-backoff"));
        this.maxBackoff = Duration.ofMillis(config.getInt("app.prometheus-push-max-backoff"));
        this.randomFactor = config.getDouble("app.prometheus-push-random-factor");
        this.maxRetries = config.getInt("app.prometheus-push-max-retries");
        if (metricsEnabled) {
            this.httpClient = httpClient.httpClientFor(prometheusPushGateway);
            this.sourceQueue = Source.<PrometheusMetric>queue(streamBufferSize)
                    .throttle(maxSamplesPerSec, Duration.ofSeconds(1))
                    .via(protoFlow())
                    .via(retryFlow())
                    .to(Sink.ignore())
                    .run(materializer);
        }
    }

    private Flow<PrometheusMetric, byte[], NotUsed> protoFlow() {
        return Flow.<PrometheusMetric>create()
                .map(metric -> {
                    // build labels
                    var labelBuilder = Label.newBuilder();
                    labelBuilder.setName("_name_");
                    labelBuilder.setValue(METRIC_PREFIX.concat(metric.name()));
                    metric.labels().keySet().forEach(keyValue -> {
                        labelBuilder.setName(keyValue);
                        labelBuilder.setValue(metric.labels().get(keyValue));
                    });
                    // build sample
                    var sampleBuilder = Sample.newBuilder();
                    if (metric.timeStamp().isPresent()) {
                        sampleBuilder.setTimestamp(metric.timeStamp().get());
                    } else {
                        sampleBuilder.setTimestamp(System.currentTimeMillis());
                    }
                    sampleBuilder.setValue(metric.value());
                    // assemble TimeSeries
                    var timeSeries = TimeSeries.newBuilder();
                    timeSeries.addLabels(labelBuilder.build());
                    timeSeries.addSamples(sampleBuilder.build());
                    // assemble WriteRequest
                    var writeRequest = WriteRequest.newBuilder();
                    return Snappy.compress(
                            writeRequest.addTimeseries(timeSeries.build()).build().toByteArray()
                    );
                });
    }

    private Flow<byte[], Integer, NotUsed> senderFlow() {
        return Flow.<byte[]>create()
            .mapAsync(1, metricPayload -> {
/*
                    The following headers MUST be sent with the HTTP request:

                    Content-Encoding: snappy
                    Content-Type: application/x-protobuf
                    User-Agent: <name & version of the sender>
                    X-Prometheus-Remote-Write-Version: 0.1.0
*/
                CompletionStage<StrictResponse<ByteString>> asyncResponse =
                        httpClient.POST("/metrics")
                                .addHeader("Content-Encoding", "snappy")
                                .addHeader("Content-Type", "application/x-protobuf")
                                .addHeader("User-Agent", "akka-cache v0.0.1")
                                .addHeader("X-Prometheus-Remote-Write-Version", "0.1.0")
                                .invokeAsync();
                return asyncResponse.thenApply(response -> response.status().intValue());
            });
    }

    Flow<byte[], Integer, NotUsed> retryFlow() {
        return RetryFlow.withBackoff(
                this.minBackoff,
                this.maxBackoff,
                this.randomFactor,
                this.maxRetries,
                senderFlow(),
                (metric, status) -> (status == 429) ? Optional.of(metric) : Optional.empty()
        );
    }

    public void logMetric(PrometheusMetric metric) {
        if (metricsEnabled) {
            QueueOfferResult result = sourceQueue.offer(metric);
            if (log.isDebugEnabled()) {
                if (result == QueueOfferResult.enqueued()) {
                    log.debug("enqueued {}", metric.name());
                }
                else if (result == QueueOfferResult.dropped()) {
                    log.debug("dropped {}", metric.name());
                }
                else if (result instanceof QueueOfferResult.Failure) {
                    QueueOfferResult.Failure failure = (QueueOfferResult.Failure) result;
                    log.debug("Offer failed {}", failure.cause().getMessage());
                }
                else if (result instanceof QueueOfferResult.QueueClosed$) {
                    log.debug("Bounded Source Queue closed");
                }
            }
        }
    }

    @Override
    public DependencyProvider createDependencyProvider() {
        return new DependencyProvider() {
            @Override
            public <T> T getDependency(Class<T> clazz) {
                if (clazz == PrometheusMetrics.class) {
                    return (T) this;
                } else {
                    throw new RuntimeException("No such dependency found: " + clazz);
                }
            }
        };
    }
}
