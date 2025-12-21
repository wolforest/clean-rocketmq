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

    public abstract void initConfig();
    public abstract void initBenchmark();

    public void benchmark() {
        for (int i = 0; i < benchmarkList.size(); i++) {
            Benchmark benchmark = benchmarkList.get(i);

            benchmark.benchmark();
            reportList.set(i, benchmark.getReport());

            benchmark.cleanup();
        }
    }

    public void showReport() {
        showHeader();

        for (int i = 0; i < reportList.size(); i++) {
            Config config = configList.get(i);
            Report report = reportList.get(i);
            System.out.printf(
                "| %-12d | %-12d | %-4d  | %-4d | %-6.2f | %-9.2f |\n",
                config.getConcurrency(),
                config.getRequestNumber(),
                report.getTps(),
                report.getQps(),
                report.getMaxRT(),
                report.getAverageRT()
            );
        }
    }

    private void showHeader() {
        String header = "| concurrency | requestNumber | tps | qps | maxRT | averageRT |";
        String separator = "| ---------- | ------------ | --- | --- | ----- | --------- |";
        System.out.println(header);
        System.out.println(separator);
    }
}
