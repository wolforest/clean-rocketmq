package com.wolf.minimq.store.domain.queue.store;

import com.wolf.minimq.domain.enums.QueueType;
import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.service.store.domain.QueueStore;
import java.util.List;
import lombok.Getter;

public class DefaultQueueStore implements QueueStore {
    @Getter
    private final String topic;
    @Getter
    private final int queueId;
    private final String rootPath;
    private final int fileSize;

    public DefaultQueueStore(String topic, int queueId, String rootPath, int fileSize) {
        this.topic = topic;
        this.queueId = queueId;
        this.rootPath = rootPath;
        this.fileSize = fileSize;
    }

    @Override
    public QueueType getQueueType() {
        return QueueType.DEFAULT;
    }

    @Override
    public void enqueue(CommitLogEvent event) {

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
        return 0;
    }

    @Override
    public long getMaxOffset() {
        return 0;
    }

    @Override
    public long getCurrentOffset() {
        return 0;
    }

    @Override
    public long increaseOffset() {
        return 0;
    }
}
