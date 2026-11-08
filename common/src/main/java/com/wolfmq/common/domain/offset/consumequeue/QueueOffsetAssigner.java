package com.wolfmq.common.domain.offset.consumequeue;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.collection.MapUtil;
import com.wolfmq.common.domain.offset.checkpoint.store.CheckpointStore;
import com.wolfmq.common.domain.offset.utils.KeyUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class QueueOffsetAssigner implements Lifecycle {
    private final CheckpointStore checkpoint;
    private final ConcurrentMap<String, AtomicLong> offsetMap;

    public QueueOffsetAssigner(CheckpointStore checkpoint) {
        this.checkpoint = checkpoint;
        this.offsetMap = new ConcurrentHashMap<>(1024);
    }

    public long assign(String topic, int queueId) {
        return assign(topic, queueId, 1);
    }

    public long assign(String topic, int queueId, int num) {
        String key = KeyUtils.buildQueueKey(topic, queueId);

        long newOffset = offsetMap.computeIfAbsent(
            key, _ -> new AtomicLong(0)
        ).addAndGet(num);

        checkpoint.updateQueueOffset(topic, queueId, newOffset);
        return newOffset;
    }

    public void updateQueueOffset(String topic, int queueId, long queueOffset) {
        String key = KeyUtils.buildQueueKey(topic, queueId);

        offsetMap.computeIfAbsent(
            key, _ -> new AtomicLong(0)
        ).set(queueOffset);

        checkpoint.updateQueueOffset(topic, queueId, queueOffset);
    }

    public Long getQueueOffset(String topic, int queueId) {
        String key = KeyUtils.buildQueueKey(topic, queueId);

        return offsetMap.computeIfAbsent(
            key, _ -> new AtomicLong(0)
        ).get();
    }

    @Override
    public void initialize() throws Exception {
        Map<String, Long> map = checkpoint.getQueueOffsetMap();
        if (MapUtil.isEmpty(map)) {
            return;
        }

        for (Map.Entry<String, Long> entry : map.entrySet()) {
            offsetMap.put(
                entry.getKey(),
                new AtomicLong(entry.getValue())
            );
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
