package com.wolfmq.test.e2e.benchmark.produce;

import com.wolfmq.test.e2e.benchmark.core.Benchmark;
import com.wolfmq.test.e2e.benchmark.core.BenchmarkSuit;
import com.wolfmq.test.e2e.benchmark.core.Config;
import com.wolfmq.test.e2e.benchmark.core.ConfigBuilder;
import java.util.ArrayList;
import java.util.List;

public class ProduceBenchmarkSuit extends BenchmarkSuit {
    @Override
    public void initConfig() {
        int topicNumber = 10;
        int messageSize = 1024;
        int requestNumber = 1_000_000;
        List<Integer> concurrencyList = List.of(
          10, 12, 15, 18, 20, 25, 30
        );

        this.configList = new ConfigBuilder()
            .requestNumber(requestNumber)
            .topicNumber(topicNumber)
            .messageSize(messageSize)
            .concurrencyList(concurrencyList)
            .build();
    }

    @Override
    public void initBenchmark(Config config) {
        this.benchmarkList = new ArrayList<>();

        for (int i = 0; i < config.getConcurrency(); i++) {
            Benchmark benchmark = new ProduceBenchmark();
            benchmarkList.add(benchmark);

            benchmark.prepare(config);
        }
    }
}
