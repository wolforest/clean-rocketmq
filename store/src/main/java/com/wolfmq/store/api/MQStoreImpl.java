package com.wolfmq.store.api;

import com.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import com.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import com.wolfmq.domain.domain.consumer.ack.store.CheckPointRequest;
import com.wolfmq.domain.domain.consumer.ack.store.OffsetRequest;
import com.wolfmq.domain.domain.mq.dto.DequeueRequest;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import com.wolfmq.domain.domain.mq.dto.EnqueueRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.server.store.api.MQStore;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.domain.domain.mq.MQService;
import com.wolfmq.store.domain.mq.ack.AckService;
import com.wolfmq.store.domain.mq.ack.InvisibleService;
import java.util.concurrent.CompletableFuture;

public class MQStoreImpl implements MQStore {
    private final MQService mqService;
    private final AckService ackService;
    private final InvisibleService invisibleService;
    private final ConsumeQueueManager consumeQueueManager;

    public MQStoreImpl(
        MQService mqService,
        AckService ackService,
        InvisibleService invisibleService,
        ConsumeQueueManager consumeQueueManager
    ) {
        this.ackService = ackService;
        this.invisibleService = invisibleService;

        this.mqService = mqService;
        this.consumeQueueManager = consumeQueueManager;
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
        return mqService.getMessage(request);
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
        long minOffset = consumeQueueManager.getMinOffset(
            request.getTopicName(),
            request.getQueueId()
        );

        return QueueResult.minOffset(minOffset);
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        long maxOffset = consumeQueueManager.getMaxOffset(
            request.getTopicName(),
            request.getQueueId()
        );

        return QueueResult.maxOffset(maxOffset);
    }
}
