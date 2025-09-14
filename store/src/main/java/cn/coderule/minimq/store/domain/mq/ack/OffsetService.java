package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.cluster.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.domain.cluster.store.domain.meta.ConsumeOrderService;
import cn.coderule.minimq.domain.domain.consumer.ack.AckConverter;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OffsetService {
    private final DequeueLock dequeueLock;
    private final ConsumeOffsetService consumeOffsetService;
    private final ConsumeOrderService consumeOrderService;
    private final InflightCounter inflightCounter;

    public OffsetService(
        DequeueLock dequeueLock,
        InflightCounter inflightCounter,
        ConsumeOffsetService consumeOffsetService,
        ConsumeOrderService consumeOrderService
    ) {
        this.dequeueLock = dequeueLock;
        this.consumeOffsetService = consumeOffsetService;
        this.consumeOrderService = consumeOrderService;
        this.inflightCounter = inflightCounter;
    }

    public void ack(AckMessage ackMessage) {
        if (!ackMessage.isConsumeOrderly()) return;
        if (isOffsetOld(ackMessage)) return;

        lock(ackMessage);
        try {
            updateOffset(ackMessage);
            decreaseCounter(ackMessage);
        } finally {
            unlock(ackMessage);
        }
    }

    public AckResult changeInvisible(AckMessage ackMessage) {
        if (!ackMessage.isConsumeOrderly()) return AckResult.failure();
        if (isOffsetOld(ackMessage)) return AckResult.failure();

        lock(ackMessage);
        try {
            return changeInvisibleWithLock(ackMessage);
        } finally {
            unlock(ackMessage);
        }
    }

    public AckResult changeInvisibleWithLock(AckMessage ackMessage) {
        return null;
    }

    private boolean isOffsetOld(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        long ackOffset = ackInfo.getAckOffset();

        long offset = consumeOffsetService.getOffset(
            ackInfo.getConsumerGroup(),
            ackInfo.getTopic(),
            ackInfo.getQueueId()
        );

        return ackOffset < offset;
    }

    private void updateOffset(AckMessage ackMessage) {
        if (isOffsetOld(ackMessage)) {
            return;
        }

        OrderRequest request = AckConverter.toOrderRequest(ackMessage);
        long nextOffset = consumeOrderService.commit(request);
        if (nextOffset < 0) {
            log.warn("commit order failed, request: {}", request);
            return;
        }

        AckInfo ackInfo = ackMessage.getAckInfo();
        updateOffset(ackInfo, nextOffset);
    }

    private void updateOffset(AckInfo ackInfo, long nextOffset) {
        boolean hasResetOffset = consumeOffsetService.containsResetOffset(
            ackInfo.getConsumerGroup(),
            ackInfo.getTopic(),
            ackInfo.getQueueId()
        );

        if (hasResetOffset) {
            return;
        }

        consumeOffsetService.putOffset(
            ackInfo.getConsumerGroup(),
            ackInfo.getTopic(),
            ackInfo.getQueueId(),
            nextOffset
        );
    }

    private void decreaseCounter(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        inflightCounter.decrement(
            ackInfo.getTopic(),
            ackInfo.getConsumerGroup(),
            ackInfo.getPopTime(),
            ackInfo.getQueueId(),
            1
        );
    }

    private void lock(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        dequeueLock.lock(
            ackInfo.getTopic(),
            ackInfo.getConsumerGroup(),
            ackInfo.getQueueId()
        );
    }

    private void unlock(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        dequeueLock.unlock(
            ackInfo.getTopic(),
            ackInfo.getConsumerGroup(),
            ackInfo.getQueueId()
        );
    }
}
