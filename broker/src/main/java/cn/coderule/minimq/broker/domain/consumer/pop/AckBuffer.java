package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPointWrapper;
import cn.coderule.minimq.domain.domain.model.consumer.pop.QueueWithTime;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class AckBuffer implements Serializable {
    private String reviveTopic;

    private final AtomicInteger counter;
    private final List<Byte> ackIndexList;
    private final ConcurrentMap<String, PopCheckPointWrapper> buffer;
    private final ConcurrentMap<String, QueueWithTime<PopCheckPointWrapper>> commitOffsets;


    public AckBuffer() {
        this.counter = new AtomicInteger(0);
        this.buffer = new ConcurrentHashMap<>(16 * 1024);
        this.commitOffsets = new ConcurrentHashMap<>();
        this.ackIndexList = new ArrayList<>(32);
    }

    public long getLatestOffset(String lockKey) {
        QueueWithTime<PopCheckPointWrapper> queue = this.commitOffsets.get(lockKey);
        if (queue == null) {
            return -1;
        }
        PopCheckPointWrapper pointWrapper = queue.get().peekLast();
        if (pointWrapper != null) {
            return pointWrapper.getNextBeginOffset();
        }
        return -1;
    }

    public int getQueueSize(String lockKey) {
        QueueWithTime<PopCheckPointWrapper> queue = this.commitOffsets.get(lockKey);
        if (queue == null) {
            return 0;
        }

        return queue.get().size();
    }

}
