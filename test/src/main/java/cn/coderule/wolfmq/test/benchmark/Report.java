package cn.coderule.wolfmq.test.benchmark;

import java.io.Serializable;
import lombok.Data;

@Data
public class Report implements Serializable {
    private int tps = 0;
    private int qps = 0;

    private double maxRT = 0.0;
    private double averageRT = 0.0;
}
