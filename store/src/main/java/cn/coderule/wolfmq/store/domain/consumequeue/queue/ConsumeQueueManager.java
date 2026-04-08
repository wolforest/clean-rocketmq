package cn.coderule.wolfmq.store.domain.consumequeue.queue;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.QueueUnit;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import java.util.List;

/**
 * @renamed from ConsumeQueueFacade to ConsumeQueueManager
 * @renamed from ConsumeQueueGateway to ConsumeQueueFacade
 */
public class ConsumeQueueManager {
    private final ConsumeQueueFactory consumeQueueFactory;

    public ConsumeQueueManager(ConsumeQueueFactory consumeQueueFactory) {
        this.consumeQueueFactory = consumeQueueFactory;
    }

    public void enqueue(CommitEvent event) {
        String topic = event.getMessageBO().getTopic();
        int queueId = event.getMessageBO().getQueueId();

        getQueueStore(topic, queueId).enqueue(event);
    }

    public QueueUnit get(String topic, int queueId, long offset) {
        return getQueueStore(topic, queueId).get(offset);
    }

    public List<QueueUnit> get(String topic, int queueId, long offset, int num) {
        return getQueueStore(topic, queueId).get(offset, num);
    }

    public long assignOffset(String topic, int queueId) {
        return increaseOffset(topic, queueId);
    }

    public long increaseOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).increaseOffset();
    }

    public long getMinOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).getMinOffset();
    }

    public long getMaxOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).getMaxOffset();
    }

    public long rollToOffset(String topic, int queueId, long offset) {
        return getQueueStore(topic, queueId).rollToOffset(offset);
    }

    public boolean existsQueue(String topic, int queueId) {
        return consumeQueueFactory.exists(topic, queueId);
    }

    public void deleteByTopic(String topicName) {

    }

    private ConsumeQueue getQueueStore(String topic, int queueId) {
        return consumeQueueFactory.getOrCreate(topic, queueId);
    }
}
