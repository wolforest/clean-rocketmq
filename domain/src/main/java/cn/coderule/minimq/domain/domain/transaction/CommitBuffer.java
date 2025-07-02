package cn.coderule.minimq.domain.domain.transaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommitBuffer {
    private static final int DEFAULT_QUEUE_LENGTH = 20_000;

    private final ConcurrentMap<Integer, OffsetQueue> queueMap;

    public CommitBuffer() {
        this.queueMap = new ConcurrentHashMap<>();
    }

    public OffsetQueue getQueue(int queueId) {
        OffsetQueue offsetQueue = queueMap.get(queueId);
        if (offsetQueue != null) {
            return offsetQueue;
        }

        offsetQueue = new OffsetQueue(System.currentTimeMillis(), DEFAULT_QUEUE_LENGTH);
        OffsetQueue old = queueMap.putIfAbsent(queueId, offsetQueue);

        return old != null ? old : offsetQueue;
    }
}
