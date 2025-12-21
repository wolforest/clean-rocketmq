package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import lombok.Data;

@Data
public class Report implements Serializable {
    private double tps = 0;
    private double qps = 0;

    private int successCount = 0;
    private int failureCount = 0;

    private double maxRT = 0.0;
    private double averageRT = 0.0;

    private double errorRate = 0.0;

    private long startTime = 0;
    private long endTime = 0;
    private long elapsedTime = 0;

    public void calculate() {
        elapsedTime = endTime - startTime;
        qps = successCount * 1000.0 / elapsedTime;
        tps = (successCount + failureCount) * 1000.0 / elapsedTime;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void increaseSuccessCount(int count) {
        successCount += count;
    }

    public void increaseFailureCount(int count) {
        failureCount += count;
    }

    public void increaseSuccessCount() {
        successCount++;
    }

    public void increaseFailureCount() {
        failureCount++;
    }

    public void stop() {
        endTime = System.currentTimeMillis();
    }
}
