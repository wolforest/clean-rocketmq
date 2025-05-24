package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPointWrapper;
import cn.coderule.minimq.domain.domain.model.consumer.pop.QueueWithTime;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
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
        PopCheckPointWrapper pointWrapper = queue.getQueue().peekLast();
        if (pointWrapper != null) {
            return pointWrapper.getNextBeginOffset();
        }
        return -1;
    }

    public void clearOffset(String lockKey) {
        this.commitOffsets.remove(lockKey);
    }

    public int getQueueSize(String lockKey) {
        QueueWithTime<PopCheckPointWrapper> queue = this.commitOffsets.get(lockKey);
        if (queue == null) {
            return 0;
        }

        return queue.getQueue().size();
    }

    public int getTotalSize() {
        int total = 0;
        for (Map.Entry<String, QueueWithTime<PopCheckPointWrapper>> entry : this.commitOffsets.entrySet()) {
            LinkedBlockingDeque<PopCheckPointWrapper> queue = entry.getValue().getQueue();
            total += queue.size();
        }

        return total;
    }

    public void enqueue(PopCheckPointWrapper pointWrapper) {
        QueueWithTime<PopCheckPointWrapper> queue = this.commitOffsets.computeIfAbsent(
            pointWrapper.getLockKey(),
            k -> new QueueWithTime<>()
        );

        queue.setTime(pointWrapper.getCk().getPopTime());
        queue.getQueue().offer(pointWrapper);
        this.buffer.put(pointWrapper.getMergeKey(), pointWrapper);
        this.counter.incrementAndGet();
    }

    public int getCount() {
        return this.counter.get();
    }

    public boolean containsKey(String lockKey) {
        return this.buffer.containsKey(lockKey);
    }

}
