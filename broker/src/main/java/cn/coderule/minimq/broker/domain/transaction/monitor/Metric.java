package cn.coderule.minimq.broker.domain.transaction.monitor;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

@Data
public class Metric implements Serializable {
    private AtomicLong count;
    private long timeStamp;

    public Metric() {
        count = new AtomicLong(0);
        timeStamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", count.get(), timeStamp);
    }
}
