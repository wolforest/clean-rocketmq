package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.domain.cluster.store.QueueUnit;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import java.util.List;

public class DefaultConsumeQueueGateway implements ConsumeQueueGateway {
    private final ConsumeQueueFactory consumeQueueFactory;

    public DefaultConsumeQueueGateway(ConsumeQueueFactory consumeQueueFactory) {
        this.consumeQueueFactory = consumeQueueFactory;
    }

    @Override
    public void enqueue(CommitEvent event) {
        String topic = event.getMessageBO().getTopic();
        int queueId = event.getMessageBO().getQueueId();

        getQueueStore(topic, queueId).enqueue(event);
    }

    @Override
    public QueueUnit get(String topic, int queueId, long offset) {
        return getQueueStore(topic, queueId).get(offset);
    }

    @Override
    public List<QueueUnit> get(String topic, int queueId, long offset, int num) {
        return getQueueStore(topic, queueId).get(offset, num);
    }

    @Override
    public long assignOffset(String topic, int queueId) {
        return increaseOffset(topic, queueId);
    }

    @Override
    public long increaseOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).increaseOffset();
    }

    @Override
    public long getMinOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).getMinOffset();
    }

    @Override
    public long getMaxOffset(String topic, int queueId) {
        return getQueueStore(topic, queueId).getMaxOffset();
    }

    @Override
    public long rollToOffset(String topic, int queueId, long offset) {
        return 0;
    }

    @Override
    public boolean existsQueue(String topic, int queueId) {
        return consumeQueueFactory.exists(topic, queueId);
    }

    @Override
    public void deleteByTopic(String topicName) {

    }

    private ConsumeQueue getQueueStore(String topic, int queueId) {
        return consumeQueueFactory.getOrCreate(topic, queueId);
    }
}
