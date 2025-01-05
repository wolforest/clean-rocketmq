package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;

public class ConsumeQueueFactory {
    private final TopicStore topicStore;

    public ConsumeQueueFactory(TopicStore topicStore) {
        this.topicStore = topicStore;
    }

    public ConsumeQueue getOrCreate(String topic, int queueId) {
        if (!topicStore.exists(topic)) {
            return ErrorConsumeQueue.singleton();
        }

        return null;
    }

}
