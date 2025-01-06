package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.config.ConsumeQueueConfig;
import com.wolf.minimq.domain.enums.QueueType;
import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.store.infra.file.DefaultMappedFileQueue;
import com.wolf.minimq.store.server.StoreCheckpoint;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultConsumeQueue implements ConsumeQueue {
    @Getter
    private final String topic;
    @Getter
    private final int queueId;
    @Getter
    private MappedFileQueue mappedFileQueue;
    private final ConsumeQueueConfig config;
    private final StoreCheckpoint checkpoint;
    private final ByteBuffer writeBuffer;

    private long maxOffset = -1;
    private volatile long minOffset = 0;

    public DefaultConsumeQueue(String topic, int queueId, ConsumeQueueConfig config, StoreCheckpoint checkpoint) {
        this.topic = topic;
        this.queueId = queueId;
        this.config = config;
        this.checkpoint = checkpoint;

        this.writeBuffer = ByteBuffer.allocate(config.getUnitSize());
        initMappedFileQueue();
    }

    @Override
    public QueueType getQueueType() {
        return QueueType.DEFAULT;
    }

    @Override
    public void enqueue(CommitLogEvent event) {
        for (int i = 0; i < config.getMaxEnqueueRetry(); i++) {
            boolean success = insert(event);
            if (success) {
                postEnqueue(event);
                return;
            }
        }
    }

    private boolean insert(CommitLogEvent event) {

        return true;
    }

    private void postEnqueue(CommitLogEvent event) {
        checkpoint.setConsumeQueueFlushTime(event.getMessageBO().getStoreTimestamp());
    }

    @Override
    public QueueUnit fetch(long offset) {
        return null;
    }

    @Override
    public List<QueueUnit> fetch(long offset, int num) {
        return List.of();
    }

    @Override
    public long getMinOffset() {
        return this.minOffset;
    }

    @Override
    public long getMaxOffset() {
        return this.maxOffset;
    }

    @Override
    public long increaseOffset() {
        return 0;
    }

    private void initMappedFileQueue() {
        String path = config.getRootPath()
            + File.separator
            + topic
            + File.separator
            + queueId;
        this.mappedFileQueue = new DefaultMappedFileQueue(path, config.getFileSize());
    }

}
