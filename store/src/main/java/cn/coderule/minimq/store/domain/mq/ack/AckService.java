package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.model.consumer.pop.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckService {
    private final StoreConfig storeConfig;
    private final String reviveTopic;
    private final long interval = 5_000;

    private final AckBuffer ackBuffer;

    public AckService(StoreConfig storeConfig, String reviveTopic, AckBuffer ackBuffer) {
        this.storeConfig  = storeConfig;
        this.reviveTopic = reviveTopic;

        this.ackBuffer = ackBuffer;
    }

    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset) {

    }

    public void ack(AckMsg ackMsg, int reviveQueueId) {

    }

    public long getLatestOffset(String topic, String group, int queueId) {
        return ackBuffer.getLatestOffset(KeyBuilder.buildConsumeKey(topic, group, queueId));
    }

    public int getTotalSize() {
        return ackBuffer.getTotalSize();
    }

    public int getBufferedSize() {
        return ackBuffer.getCount();
    }

}
