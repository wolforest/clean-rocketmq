package com.wolfmq.test.e2etest.benchmark.core;

import java.util.concurrent.CountDownLatch;

public interface Benchmark {
    void prepare(Config config);
    void benchmark();
    void cleanup();
    Report getReport();
    default void setStartLatch(CountDownLatch latch) {}
}
