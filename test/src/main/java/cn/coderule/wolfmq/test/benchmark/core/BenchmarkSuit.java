package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract class BenchmarkSuit implements Serializable {
    protected List<Config> configList = new ArrayList<>();
    protected List<Report> reportList = new ArrayList<>();
    protected List<Benchmark> benchmarkList = new ArrayList<>();

    public abstract void initConfig();
    public abstract void initBenchmark(Config config);

    public void benchmark() {
        for (Config config : configList) {
            this.initBenchmark(config);

            this.concurrentBenchmark();

            Report report = this.calculateReport();
            reportList.add(report);
        }

    }

    private void concurrentBenchmark() {
        List<Thread> threadList = new ArrayList<>();
        for (Benchmark benchmark : benchmarkList) {
            Runnable task = benchmark::benchmark;

            Thread thread = new Thread(task);
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.error("concurrentBenchmark exception: ", e);
            }
        }

        for (Benchmark benchmark : benchmarkList) {
            benchmark.cleanup();
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
        } else if (report.getEndTime() > benchmarkReport.getEndTime()) {
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
            log.info("thread Report: {}", benchmarkReport);
            calculateTime(report, benchmarkReport);
            calculateRequest(report, benchmarkReport);
        }

        report.calculate();
        log.info("benchmark Report: {}", report);

        return report;
    }

    public void showReport() {
        showHeader();

        for (int i = 0; i < reportList.size(); i++) {
            Config config = configList.get(i);
            Report report = reportList.get(i);
            System.out.printf(
                "| %-12d | %-12d | %-6.2f  | %-6.2f | %-6.2f | %-9.2f |\n",
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
