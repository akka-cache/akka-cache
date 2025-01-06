package com.akka.cache.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public sealed interface PrometheusMetricsAPI {

    record PrometheusMetric(String name, Optional<Long> timeStamp, double value, Map<String, String> labels) implements PrometheusMetricsAPI {

        public PrometheusMetric(String name, double value) {
            this(name, Optional.empty(), value, Collections.emptyMap());
        }

        public PrometheusMetric(String name, double value, Map<String, String> labels) {
            this(name, Optional.empty(), value, labels);
        }

    }
}
