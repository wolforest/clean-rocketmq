package com.wolfmq.store.domain.consumequeue.cloud;

import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.common.domain.consumequeue.ConsumeQueue;
import com.wolfmq.common.domain.consumequeue.QueueType;
import com.wolfmq.common.domain.consumequeue.QueueUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class CloudConsumeQueue implements ConsumeQueue {
    @Override
    public QueueType getQueueType() {
        return null;
    }

    @Override
    public String getTopic() {
        return "";
    }

    @Override
    public int getQueueId() {
        return 0;
    }

    @Override
    public int getUnitSize() {
        return 0;
    }

    @Override
    public void enqueue(CommitEvent event) {

    }

    @Override
    public QueueUnit get(long index) {
        return null;
    }

    @Override
    public List<QueueUnit> get(long index, int num) {
        return List.of();
    }

    @Override
    public long getMinOffset() {
        return 0;
    }

    @Override
    public void setMinOffset(long offset) {

    }

    @Override
    public long getMaxOffset() {
        return 0;
    }

    @Override
    public void setMaxOffset(long maxOffset) {

    }

    @Override
    public long increaseOffset() {
        return 0;
    }

    @Override
    public long rollToOffset(long offset) {
        return 0;
    }

    @Override
    public void setCommitOffsetMap(ConcurrentMap<Integer, Long> map) {

    }

    @Override
    public Map<Integer, Long> getCommitOffsetMap() {
        return Map.of();
    }

    @Override
    public Long getCommitOffsetByShardId(int shardId) {
        return 0L;
    }

    @Override
    public void setCommitOffsetByShardId(int shardId, long offset) {

    }

    @Override
    public long getMaxCommitOffset() {
        return 0;
    }

    @Override
    public void setMaxCommitOffset(long maxCommitOffset) {

    }

    @Override
    public MappedFileQueue getMappedFileQueue() {
        return null;
    }

    @Override
    public void load() {

    }

    @Override
    public void flush(int minPages) {

    }

    @Override
    public void destroy() {

    }
}
