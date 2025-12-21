package cn.coderule.wolfmq.test.benchmark;

import cn.coderule.wolfmq.test.benchmark.core.BenchmarkSuit;

public class ProduceBenchmarkSuit extends BenchmarkSuit {

    @Override
    public void initConfig() {

    }

    @Override
    public void initBenchmark() {

    }

    public static void main(String[] args) {
        ProduceBenchmarkSuit suit = new ProduceBenchmarkSuit();
        suit.initConfig();
        suit.initBenchmark();

        suit.benchmark();
        suit.showReport();
    }
}
