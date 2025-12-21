package cn.coderule.wolfmq.test.benchmark.produce;

import cn.coderule.wolfmq.test.benchmark.core.Benchmark;
import cn.coderule.wolfmq.test.benchmark.core.BenchmarkSuit;
import cn.coderule.wolfmq.test.benchmark.core.Config;
import cn.coderule.wolfmq.test.benchmark.core.ConfigBuilder;
import java.util.List;

public class ProduceBenchmarkSuit extends BenchmarkSuit {
    @Override
    public void initConfig() {
        int topicNumber = 10;
        int messageSize = 1024;
        List<Integer> concurrencyList = List.of(
            50, 100
        );

        this.configList = new ConfigBuilder()
            .topicNumber(topicNumber)
            .messageSize(messageSize)
            .concurrencyList(concurrencyList)
            .build();
    }

    @Override
    public void initBenchmark(Config config) {
        for (int i = 0; i < configList.size(); i++) {
            Benchmark benchmark = new ProduceBenchmark();
            benchmarkList.add(benchmark);

            benchmark.prepare(config);
        }
    }
}
