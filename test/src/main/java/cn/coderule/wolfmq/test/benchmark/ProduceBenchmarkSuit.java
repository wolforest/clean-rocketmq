package cn.coderule.wolfmq.test.benchmark;

import cn.coderule.wolfmq.test.benchmark.core.Benchmark;
import cn.coderule.wolfmq.test.benchmark.core.BenchmarkSuit;
import cn.coderule.wolfmq.test.benchmark.core.Config;

public class ProduceBenchmarkSuit extends BenchmarkSuit {

    @Override
    public void initConfig() {

    }

    @Override
    public void initBenchmark() {
        for (int i = 0; i < configList.size(); i++) {
            Config config = configList.get(i);

            Benchmark benchmark = new ProduceBenchmark();
            benchmarkList.set(i, benchmark);

            benchmark.prepare(config);
        }
    }

    public static void main(String[] args) {
        ProduceBenchmarkSuit suit = new ProduceBenchmarkSuit();
        suit.initConfig();
        suit.initBenchmark();

        suit.benchmark();
        suit.showReport();
    }
}
