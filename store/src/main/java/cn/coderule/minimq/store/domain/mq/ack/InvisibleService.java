package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.AckBuffer;
import cn.coderule.minimq.domain.domain.consumer.ack.AckConverter;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvisibleService {
    private final StoreConfig storeConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;

    private final AckBuffer ackBuffer;
    private final OffsetService offsetService;
    private final EnqueueService enqueueService;

    public InvisibleService(
        StoreConfig storeConfig,
        String reviveTopic,
        AckBuffer ackBuffer,
        EnqueueService enqueueService,
        OffsetService offsetService
    ) {
        this.storeConfig  = storeConfig;
        this.messageConfig  = storeConfig.getMessageConfig();
        this.reviveTopic = reviveTopic;

        this.ackBuffer = ackBuffer;
        this.enqueueService = enqueueService;
        this.offsetService = offsetService;
    }

    public AckResult changeInvisible(AckMessage ackMessage) {
        if (ackMessage.isConsumeOrderly()) {
            return offsetService.changeInvisible(ackMessage);
        }

        long now = System.currentTimeMillis();
        AckResult result = AckConverter.toAckResult(ackMessage, now);

        if (!appendCheckpoint(ackMessage, now)) {
            return result.appendCheckpointFailure();
        }

        return ackOriginal(ackMessage, result);
    }

    private boolean appendCheckpoint(AckMessage ackMessage, long now) {
        PopCheckPoint checkPoint = AckConverter.toCheckpoint(ackMessage, now);
        MessageBO message = AckConverter.toMessage(ackMessage, checkPoint, reviveTopic, storeConfig.getHostAddress());

        EnqueueResult result = enqueueService.enqueue(message);
        return result.isSuccess();
    }

    private AckResult ackOriginal(AckMessage ackMessage, AckResult result) {

        return result;
    }


}
