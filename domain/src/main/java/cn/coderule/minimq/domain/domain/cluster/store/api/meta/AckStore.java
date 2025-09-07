package cn.coderule.minimq.domain.domain.cluster.store.api.meta;

import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;

public interface AckStore {
    void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset);
    void ack(AckInfo ackInfo, int reviveQueueId, long invisibleTime);
    long getLatestOffset(String topic, String group, int queueId);
}
