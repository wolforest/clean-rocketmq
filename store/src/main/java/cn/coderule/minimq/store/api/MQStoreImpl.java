package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.consumer.ack.store.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.store.domain.mq.ack.AckService;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MQStoreImpl implements MQStore {
    private final MQService mqService;
    private final AckService ackService;
    private final ConsumeQueueGateway consumeQueueGateway;

    public MQStoreImpl(MQService mqService, AckService ackService, ConsumeQueueGateway consumeQueueGateway) {
        this.mqService = mqService;
        this.ackService = ackService;
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
    public MessageBO getMessage(String topic, int queueId, long offset) {
        return mqService.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        return mqService.getMessage(topic, queueId, offset, num);
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
    public void ack(AckRequest request) {
        ackService.ack(
            request.getAckMsg(),
            request.getReviveQueueId(),
            request.getInvisibleTime()
        );
    }

    @Override
    public long getBufferedOffset(OffsetRequest request) {
        return ackService.getBufferedOffset(
            request.getTopicName(),
            request.getGroupName(),
            request.getQueueId()
        );
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
