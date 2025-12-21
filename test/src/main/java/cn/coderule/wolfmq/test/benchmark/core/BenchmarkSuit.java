package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public abstract class BenchmarkSuit implements Serializable {
    protected final List<Config> configList = new ArrayList<>();
    protected final List<Report> reportList = new ArrayList<>();
    protected final List<Benchmark> benchmarkList = new ArrayList<>();

    public abstract void prepare();

    public void benchmark() {
        for (int i = 0; i < benchmarkList.size(); i++) {
            Benchmark benchmark = benchmarkList.get(i);

            benchmark.benchmark();
            reportList.set(i, benchmark.getReport());

            benchmark.cleanup();
        }
    }
}
