package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPointWrapper;
import cn.coderule.minimq.domain.domain.model.consumer.pop.QueueWithTime;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class AckBuffer implements Serializable {
    private AtomicInteger counter;
    private String reviveTopic;

    private final List<Byte> ackIndexList;
    private ConcurrentMap<String, PopCheckPointWrapper> buffer;
    private ConcurrentMap<String, QueueWithTime<PopCheckPointWrapper>> commitOffsets;
}
