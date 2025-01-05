package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConsumeQueueFactory {
    private static final int TOPIC_MAP_SIZE = 32;
    private static final int QUEUE_MAP_SIZE = 128;

    private final TopicStore topicStore;
    private final ConsumeQueueFlusher flusher;
    private final ConsumeQueueLoader loader;
    /**
     * consume queue map, structure:
     * - topic
     *  - queueId
     *    - consumeQueue
     */
    protected final ConcurrentMap<String, ConcurrentMap<Integer, ConsumeQueue>> topicMap;

    public ConsumeQueueFactory(
        TopicStore topicStore,
        ConsumeQueueFlusher flusher,
        ConsumeQueueLoader loader
    ) {
        this.topicStore = topicStore;
        this.flusher = flusher;
        this.loader = loader;

        this.topicMap = new ConcurrentHashMap<>(TOPIC_MAP_SIZE);
    }

    public void createAll() {

    }

    public ConsumeQueue getOrCreate(String topic, int queueId) {
        if (!topicStore.exists(topic)) {
            return ErrorConsumeQueue.singleton();
        }

        ConsumeQueue queue = get(topic, queueId);
        if (queue != null) {
            return queue;
        }

        return create(topic, queueId);
    }

    private ConsumeQueue get(String topic, int queueId) {
        Map<Integer, ConsumeQueue> queueMap = topicMap.get(topic);
        if (queueMap == null) {
            return null;
        }

        return queueMap.get(queueId);
    }

    private ConsumeQueue create(String topic, int queueId) {
        ConcurrentMap<Integer, ConsumeQueue> queueMap = initQueueMap(topic);
        if (queueMap.containsKey(queueId)) {
            return queueMap.get(queueId);
        }

        ConsumeQueue queue = createConsumeQueue(topic, queueId);
        ConsumeQueue result = queueMap.putIfAbsent(queueId, queue);

        return result == null ? queue : result;
    }

    private ConsumeQueue createConsumeQueue(String topic, int queueId) {
        return null;
    }

    private ConcurrentMap<Integer, ConsumeQueue> initQueueMap(String topic) {
        if (topicMap.containsKey(topic)) {
            return topicMap.get(topic);
        }

        ConcurrentMap<Integer, ConsumeQueue> queueMap = new ConcurrentHashMap<>(QUEUE_MAP_SIZE);
        ConcurrentMap<Integer, ConsumeQueue> resultMap = topicMap.putIfAbsent(topic, queueMap);

        return resultMap == null ? queueMap : resultMap;
    }

}
