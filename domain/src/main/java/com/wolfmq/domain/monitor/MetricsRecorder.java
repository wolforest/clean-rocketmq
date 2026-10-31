package com.wolfmq.domain.monitor;

/**
 * Records metrics by convention.
 * The metric setting (type, unit) is resolved by {@code metricName}.
 * The value is a {@code long} and may be converted to another type according to the metric setting.
 *
 * <ul>
 *   <li>{@code increment}: {@code COUNTER}</li>
 *   <li>{@code set}: {@code GAUGE}</li>
 *   <li>{@code record}: {@code HISTOGRAM}, {@code TIMER}, {@code SUMMARY}, {@code METER}</li>
 * </ul>
 */
public interface MetricsRecorder {
    void increment(String metricName, long value);

    void set(String metricName, long value);

    void record(String metricName, long value);

    default void increment(String metricName) {
        increment(metricName, 1);
    }
}
