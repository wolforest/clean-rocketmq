package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import lombok.Data;

@Data
public class BenchmarkConfig implements Serializable {
    private int concurrency = 1;
    private int requestNumber = 1;
    private int duration = 1;
    private int timeout = 1000;
}
