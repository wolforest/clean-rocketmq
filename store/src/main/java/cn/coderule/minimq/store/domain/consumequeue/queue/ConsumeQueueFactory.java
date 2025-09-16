package cn.coderule.minimq.store.domain.consumequeue.queue;

import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.minimq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConsumeQueueFactory implements ConsumeQueueRegistry {
    private static final int TOPIC_MAP_SIZE = 32;
    private static final int QUEUE_MAP_SIZE = 128;

    private final ConsumeQueueConfig config;
    private final TopicService topicService;
    private final StoreCheckpoint checkpoint;

    private final List<ConsumeQueueRegistry> createHooks = new ArrayList<>();
    /**
     * consume queue map, structure:
     * topic -> queueId -> consumeQueue
     */
    protected final ConcurrentMap<String, ConcurrentMap<Integer, ConsumeQueue>> topicMap;

    public ConsumeQueueFactory(ConsumeQueueConfig config, TopicService topicService, StoreCheckpoint checkpoint) {
        this.config = config;
        this.topicService = topicService;
        this.checkpoint = checkpoint;

        this.topicMap = new ConcurrentHashMap<>(TOPIC_MAP_SIZE);
    }

    public void createAll() {

    }

    public ConsumeQueue getOrCreate(String topic, int queueId) {
        if (!topicService.exists(topic)) {
            return ErrorConsumeQueue.singleton();
        }

        ConsumeQueue queue = get(topic, queueId);
        if (queue != null) {
            return queue;
        }

        return create(topic, queueId);
    }

    public ConsumeQueue get(String topic, int queueId) {
        Map<Integer, ConsumeQueue> queueMap = topicMap.get(topic);
        if (queueMap == null) {
            return null;
        }

        return queueMap.get(queueId);
    }

    public boolean exists(String topic, int queueId) {
        Map<Integer, ConsumeQueue> queueMap = topicMap.get(topic);
        if (queueMap == null) {
            return false;
        }

        return queueMap.containsKey(queueId);
    }

    private ConsumeQueue create(String topic, int queueId) {
        ConcurrentMap<Integer, ConsumeQueue> queueMap = initQueueMap(topic);
        if (queueMap.containsKey(queueId)) {
            return queueMap.get(queueId);
        }

        ConsumeQueue queue = createConsumeQueue(topic, queueId);
        ConsumeQueue result = queueMap.putIfAbsent(queueId, queue);

        if (result == null) {
            register(queue);
        }

        return result == null ? queue : result;
    }

    public void addCreateHook(ConsumeQueueRegistry hook) {
        createHooks.add(hook);
    }

    public void register(ConsumeQueue queue) {
        if (createHooks.isEmpty()) return;

        for (ConsumeQueueRegistry hook : createHooks) {
            hook.register(queue);
        }
    }

    private ConsumeQueue createConsumeQueue(String topic, int queueId) {
        return new DefaultConsumeQueue(topic, queueId, config, checkpoint);
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
