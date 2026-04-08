package cn.coderule.wolfmq.store.domain.mq.ack;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckConverter;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvisibleService {
    private final StoreConfig storeConfig;
    private final String reviveTopic;

    private final AckService ackService;
    private final AckOffset offsetService;
    private final EnqueueService enqueueService;

    public InvisibleService(
        StoreConfig storeConfig,
        String reviveTopic,
        AckService ackService,
        EnqueueService enqueueService,
        AckOffset offsetService
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
