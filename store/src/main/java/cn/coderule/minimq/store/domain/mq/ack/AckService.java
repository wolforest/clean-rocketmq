package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.consumer.pop.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPointWrapper;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopConverter;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.service.store.domain.MessageQueue;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckService {
    private final StoreConfig storeConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final long interval = 5_000;

    private final AckBuffer ackBuffer;
    private final MessageQueue messageQueue;

    public AckService(StoreConfig storeConfig, MessageQueue messageQueue, String reviveTopic, AckBuffer ackBuffer) {
        this.storeConfig  = storeConfig;
        this.messageConfig  = storeConfig.getMessageConfig();
        this.messageQueue = messageQueue;

        this.reviveTopic = reviveTopic;
        this.ackBuffer = ackBuffer;
    }

    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveOffset, long nextOffset) {
        PopCheckPointWrapper pointWrapper = new PopCheckPointWrapper(
            reviveQueueId,
            reviveOffset,
            point,
            nextOffset
        );

        if (ackBuffer.containsKey(pointWrapper.getMergeKey())) {
            log.warn("Duplicate checkpoint, key:{}, checkpoint: {}", pointWrapper.getMergeKey(), pointWrapper);
            return;
        }

        if (!messageConfig.isEnablePopBufferMerge()) {
            enqueueReviveQueue(pointWrapper);
        }

        ackBuffer.enqueue(pointWrapper);
    }

    public void ack(AckMsg ackMsg, int reviveQueueId) {

    }

    public long getLatestOffset(String topic, String group, int queueId) {
        String lockKey = KeyBuilder.buildConsumeKey(topic, group, queueId);
        return ackBuffer.getLatestOffset(lockKey);
    }

    public int getTotalSize() {
        return ackBuffer.getTotalSize();
    }

    public int getBufferedSize() {
        return ackBuffer.getCount();
    }

    private void enqueueReviveQueue(PopCheckPointWrapper pointWrapper) {
        if (pointWrapper.getReviveQueueOffset() >= 0) {
            return;
        }

        SocketAddress storeHost = new InetSocketAddress(storeConfig.getHost(), storeConfig.getPort());
        MessageBO messageBO = PopConverter.buildCkMsg(
            pointWrapper.getCk(),
            pointWrapper.getReviveQueueId(),
            reviveTopic,
            storeHost
        );

        EnqueueResult result = messageQueue.enqueue(messageBO);
        if (result.isFailure()) {
            log.error("Enqueue checkpoint failed, checkpoint: {}", pointWrapper);
        }

        pointWrapper.setCkStored(true);
        pointWrapper.setReviveQueueOffset(result.getQueueOffset());

        if (messageConfig.isEnablePopLog()) {
            log.info("Enqueue checkpoint success, checkpoint: {}, result: {}", pointWrapper, result);
        }
    }

}
