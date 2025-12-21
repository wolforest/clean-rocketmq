package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class Suit implements Serializable {
    private List<Config> configList;
    private List<Report> reportList;
    private List<Benchmark> benchmarkList;


}
