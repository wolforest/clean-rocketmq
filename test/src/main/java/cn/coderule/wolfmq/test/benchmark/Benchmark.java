package cn.coderule.wolfmq.test.benchmark;

public interface Benchmark {
    void prepare();
    void benchmark();
    Report getReport();
}
