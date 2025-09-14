package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.store.api.MQStore;
import cn.coderule.minimq.domain.domain.cluster.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.domain.cluster.store.domain.mq.MQService;
import cn.coderule.minimq.store.domain.mq.ack.AckService;
import cn.coderule.minimq.store.domain.mq.ack.InvisibleService;
import java.util.concurrent.CompletableFuture;

public class MQStoreImpl implements MQStore {
    private final MQService mqService;
    private final AckService ackService;
    private final InvisibleService invisibleService;
    private final ConsumeQueueGateway consumeQueueGateway;

    public MQStoreImpl(
        MQService mqService,
        AckService ackService,
        InvisibleService invisibleService,
        ConsumeQueueGateway consumeQueueGateway
    ) {
        this.ackService = ackService;
        this.invisibleService = invisibleService;

        this.mqService = mqService;
        this.consumeQueueGateway = consumeQueueGateway;
    }

    @Override
    public EnqueueResult enqueue(EnqueueRequest result) {
        return mqService.enqueue(result.getMessageBO());
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request) {
        return mqService.enqueueAsync(request.getMessageBO());
    }

    @Override
    public DequeueResult dequeue(DequeueRequest request) {
        return mqService.dequeue(request);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        return mqService.dequeueAsync(request);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        return mqService.get(request);
    }

    @Override
    public MessageResult getMessage(MessageRequest request) {
        return null;
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {
        ackService.addCheckPoint(
            request.getCheckPoint(),
            request.getReviveQueueId(),
            request.getReviveQueueOffset(),
            request.getNextOffset()
        );
    }

    @Override
    public void ack(AckMessage request) {
        ackService.ack(request);
    }

    @Override
    public AckResult changeInvisible(AckMessage request) {
        return invisibleService.changeInvisible(request);
    }

    @Override
    public QueueResult getBufferedOffset(OffsetRequest request) {
        long offset = ackService.getBufferedOffset(
            request.getGroupName(), request.getTopicName(),
            request.getQueueId()
        );

        return QueueResult.offset(offset);
    }

    @Override
    public QueueResult getMinOffset(QueueRequest request) {
        long minOffset = consumeQueueGateway.getMinOffset(
            request.getTopic(),
            request.getQueueId()
        );

        return QueueResult.minOffset(minOffset);
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        long maxOffset = consumeQueueGateway.getMaxOffset(
            request.getTopic(),
            request.getQueueId()
        );

        return QueueResult.maxOffset(maxOffset);
    }
}
