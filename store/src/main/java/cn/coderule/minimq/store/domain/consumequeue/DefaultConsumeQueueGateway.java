package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.model.CommitLogEvent;
import cn.coderule.minimq.domain.domain.model.QueueUnit;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueue;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import java.util.List;

public class DefaultConsumeQueueGateway implements ConsumeQueueGateway {
    private final ConsumeQueueFactory consumeQueueFactory;

    public DefaultConsumeQueueGateway(ConsumeQueueFactory consumeQueueFactory) {
        this.consumeQueueFactory = consumeQueueFactory;
    }

    @Override
    public void enqueue(CommitLogEvent event) {
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
        return 0L;
    }

    @Override
    public void increaseOffset(String topic, int queueId) {

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
    public void deleteByTopic(String topicName) {

    }

    private ConsumeQueue getQueueStore(String topic, int queueId) {
        return consumeQueueFactory.getOrCreate(topic, queueId);
    }
}
