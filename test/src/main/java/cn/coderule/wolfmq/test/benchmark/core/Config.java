package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config implements Serializable {
    private int concurrency = 100;
    private int requestNumber = 10000;

    private int duration = 1;
    private int timeout = 1000;

    private int topicNumber = 10;
    private int groupNumber = 10;
}
