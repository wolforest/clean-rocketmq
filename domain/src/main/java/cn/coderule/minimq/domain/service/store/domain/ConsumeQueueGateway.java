package cn.coderule.minimq.domain.service.store.domain;

import cn.coderule.minimq.domain.model.bo.CommitLogEvent;
import cn.coderule.minimq.domain.model.bo.QueueUnit;
import java.util.List;

public interface ConsumeQueueGateway {
    void enqueue(CommitLogEvent event);
    QueueUnit get(String topic, int queueId, long offset);
    List<QueueUnit> get(String topic, int queueId, long offset, int num);

    long assignOffset(String topic, int queueId);
    void increaseOffset(String topic, int queueId);

    long getMinOffset(String topic, int queueId);
    long getMaxOffset(String topic, int queueId);
}
