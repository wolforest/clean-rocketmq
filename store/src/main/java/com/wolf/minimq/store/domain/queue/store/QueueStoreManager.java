package com.wolf.minimq.store.domain.queue.store;

import com.wolf.minimq.domain.service.store.domain.QueueStore;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;

public class QueueStoreManager {
    private final TopicStore topicStore;

    public QueueStoreManager(TopicStore topicStore) {
        this.topicStore = topicStore;
    }

    public QueueStore get(String topic, int queueId) {
        if (!topicStore.exists(topic)) {
            return FackQueueStore.singleton();
        }

        return null;
    }

    public QueueStore getOrCreate(String topic, int queueId) {
        return null;
    }
}
