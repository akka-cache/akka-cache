package com.akka.cache.domain;

import akka.japi.Pair;

import java.util.Collections;
import java.util.HashMap;
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

        public PrometheusMetric newPrometheusMetric(String name, double value, Pair<String, String> label) {
            Map<String, String> newLabels = new HashMap<>();
            newLabels.put(label.first(), label.second());
            return new PrometheusMetric(name, Optional.empty(), value, newLabels);
        }

        public static PrometheusMetric newPrometheusMetric(String name, double value, Optional<String> org) {
            Map<String, String> newLabels = new HashMap<>();
            if (org.isPresent()) {
                newLabels.put("org", org.get());
            }
            return new PrometheusMetric(name, Optional.empty(), value, newLabels);
        }

        public static PrometheusMetric newPrometheusMetric(String name, Long timeStamp, double value, Optional<String> org) {
            Map<String, String> newLabels = new HashMap<>();
            if (org.isPresent()) {
                newLabels.put("org", org.get());
            }
            return new PrometheusMetric(name, Optional.of(timeStamp), value, newLabels);
        }

    }
}
