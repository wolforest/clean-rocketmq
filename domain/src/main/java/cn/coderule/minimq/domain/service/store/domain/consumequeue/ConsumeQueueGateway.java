package cn.coderule.minimq.domain.service.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.domain.cluster.store.QueueUnit;
import java.util.List;

public interface ConsumeQueueGateway {
    void enqueue(CommitEvent event);
    QueueUnit get(String topic, int queueId, long offset);
    List<QueueUnit> get(String topic, int queueId, long offset, int num);

    long assignOffset(String topic, int queueId);
    long increaseOffset(String topic, int queueId);

    long getMinOffset(String topic, int queueId);
    long getMaxOffset(String topic, int queueId);

    void deleteByTopic(String topicName);

    boolean isOffsetInCache(String topic, int queueId, long offset, int size);
}
