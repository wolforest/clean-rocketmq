package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import lombok.Data;

@Data
public class Report implements Serializable {
    private int tps = 0;
    private int qps = 0;

    private int successCount = 0;
    private int failureCount = 0;

    private double maxRT = 0.0;
    private double averageRT = 0.0;

    private double errorRate = 0.0;

    private long startTime = 0;
    private long endTime = 0;

    public void start() {
        startTime = System.currentTimeMillis();
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
