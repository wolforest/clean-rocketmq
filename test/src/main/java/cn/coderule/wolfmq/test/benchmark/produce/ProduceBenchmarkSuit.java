package cn.coderule.wolfmq.test.benchmark.produce;

import cn.coderule.wolfmq.test.benchmark.core.Benchmark;
import cn.coderule.wolfmq.test.benchmark.core.BenchmarkSuit;
import cn.coderule.wolfmq.test.benchmark.core.Config;
import java.util.List;

public class ProduceBenchmarkSuit extends BenchmarkSuit {

    @Override
    public void initConfig() {
        List<Integer> concurrencyList = List.of(
            50, 100, 200, 300, 400, 500
        );
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
}
