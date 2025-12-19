package cn.coderule.wolfmq.test.benchmark.core;

public interface Benchmark {
    void prepare();
    void benchmark();
    Report getReport();
}
