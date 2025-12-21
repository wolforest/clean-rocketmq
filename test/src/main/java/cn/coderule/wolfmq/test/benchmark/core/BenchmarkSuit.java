package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public abstract class BenchmarkSuit implements Serializable {
    protected List<Config> configList = new ArrayList<>();
    protected List<Report> reportList = new ArrayList<>();
    protected List<Benchmark> benchmarkList = new ArrayList<>();

    public abstract void initConfig();
    public abstract void initBenchmark(Config config);

    public void benchmark() {
        for (int i = 0; i < configList.size(); i++) {
            Config config = configList.get(i);

            this.benchmarkList = new ArrayList<>();
            this.initBenchmark(config);

            this.concurrentBenchmark();

            Report report = this.calculateReport();
            reportList.set(i, report);
        }

    }

    private void concurrentBenchmark() {
        for (Benchmark benchmark : benchmarkList) {
            Runnable task = () -> {
                benchmark.benchmark();
                benchmark.cleanup();
            };

            new Thread(task).start();
        }
    }

    private void calculateTime(Report report, Report benchmarkReport) {
        if (0 == report.getStartTime()) {
            report.setStartTime(benchmarkReport.getStartTime());
        } else if (report.getStartTime() > benchmarkReport.getStartTime()) {
            report.setStartTime(benchmarkReport.getStartTime());
        }

        if (0 == report.getEndTime()) {
            report.setEndTime(benchmarkReport.getEndTime());
        } else if (report.getEndTime() < benchmarkReport.getEndTime()) {
            report.setEndTime(benchmarkReport.getEndTime());
        }
    }

    private void calculateRequest(Report report, Report benchmarkReport) {
        report.increaseSuccessCount(benchmarkReport.getSuccessCount());
        report.increaseFailureCount(benchmarkReport.getFailureCount());
    }

    private Report calculateReport() {
        Report report = new Report();
        for (Benchmark benchmark : benchmarkList) {
            Report benchmarkReport = benchmark.getReport();

            calculateTime(report, benchmarkReport);
            calculateRequest(report, benchmarkReport);
        }

        report.calculate();

        return report;
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
