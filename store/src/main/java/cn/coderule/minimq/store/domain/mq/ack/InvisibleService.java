package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.minimq.domain.config.server.StoreConfig;
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
    private final String reviveTopic;

    private final AckService ackService;
    private final OffsetService offsetService;
    private final EnqueueService enqueueService;

    public InvisibleService(
        StoreConfig storeConfig,
        String reviveTopic,
        AckService ackService,
        EnqueueService enqueueService,
        OffsetService offsetService
    ) {
        this.storeConfig  = storeConfig;
        this.reviveTopic = reviveTopic;

        this.ackService = ackService;
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

        return nack(ackMessage, result);
    }

    private boolean appendCheckpoint(AckMessage ackMessage, long now) {
        PopCheckPoint checkPoint = AckConverter.toCheckpoint(ackMessage, now);
        MessageBO message = AckConverter.toMessage(ackMessage, checkPoint, reviveTopic, storeConfig.getHostAddress());

        EnqueueResult result = enqueueService.enqueue(message);
        return result.isSuccess();
    }

    private AckResult nack(AckMessage ackMessage, AckResult result) {
        ackService.nack(
            ackMessage.getAckInfo(),
            ackMessage.getReviveQueueId(),
            ackMessage.getInvisibleTime()
        );
        return result;
    }
}
