package cn.coderule.wolfmq.test.benchmark.core;

public interface Benchmark {
    void prepare(Config config);
    void benchmark();
    void cleanup();
    Report getReport();
}
