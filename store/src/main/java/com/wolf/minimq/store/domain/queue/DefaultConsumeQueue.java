package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.service.store.domain.QueueStore;
import com.wolf.minimq.store.domain.queue.store.QueueStoreManager;
import java.util.List;

public class DefaultConsumeQueue implements ConsumeQueue {
    private final QueueStoreManager queueStoreManager;

    public DefaultConsumeQueue(QueueStoreManager queueStoreManager) {
        this.queueStoreManager = queueStoreManager;
    }

    @Override
    public void enqueue(CommitLogEvent event) {
        String topic = event.getMessageBO().getTopic();
        int queueId = event.getMessageBO().getQueueId();

        getQueueStore(topic, queueId).enqueue(event);
    }

    @Override
    public QueueUnit fetch(String topic, int queueId, long offset) {
        return getQueueStore(topic, queueId).fetch(offset);
    }

    @Override
    public List<QueueUnit> fetch(String topic, int queueId, long offset, int num) {
        return getQueueStore(topic, queueId).fetch(offset, num);
    }

    @Override
    public void assignOffset(MessageBO messageBO) {

    }

    @Override
    public void increaseOffset(MessageBO messageBO) {

    }

    @Override
    public long getMinOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).getMinOffset();
    }

    @Override
    public long getMaxOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).getMaxOffset();
    }

    private QueueStore getQueueStore(String topic, int queueId) {
        return queueStoreManager.get(topic, queueId);
    }
}
