package cn.coderule.wolfmq.test.benchmark.produce;

import cn.coderule.wolfmq.test.benchmark.core.Benchmark;
import cn.coderule.wolfmq.test.benchmark.core.BenchmarkSuit;
import cn.coderule.wolfmq.test.benchmark.core.Config;
import cn.coderule.wolfmq.test.benchmark.core.ConfigBuilder;
import java.util.ArrayList;
import java.util.List;

public class ProduceBenchmarkSuit extends BenchmarkSuit {
    @Override
    public void initConfig() {
        int topicNumber = 10;
        int messageSize = 1024;
        int requestNumber = 1_000_000;
        List<Integer> concurrencyList = List.of(
          10, 15, 18, 20, 25
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
