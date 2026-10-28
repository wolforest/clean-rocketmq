package com.wolfmq.broker.infra.embed;

import com.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import com.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import com.wolfmq.domain.domain.consumer.ack.store.CheckPointRequest;
import com.wolfmq.domain.domain.consumer.ack.store.OffsetRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import com.wolfmq.domain.domain.mq.dto.EnqueueRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.domain.mq.dto.DequeueRequest;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.domain.server.store.api.MQStore;
import java.util.concurrent.CompletableFuture;

public class EmbedMQStore extends AbstractEmbedStore implements MQFacade {
    private final MQStore mqStore;

    public EmbedMQStore(MQStore mqStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.mqStore = mqStore;
    }

    @Override
    public EnqueueResult enqueue(EnqueueRequest request) {
        return mqStore.enqueue(request);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request) {
        return mqStore.enqueueAsync(request);
    }

    @Override
    public DequeueResult dequeue(DequeueRequest request) {
        return mqStore.dequeue(request);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        return mqStore.dequeueAsync(request);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        return mqStore.get(request);
    }

    @Override
    public MessageResult getMessage(MessageRequest request) {
        return mqStore.getMessage(request);
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {
        mqStore.addCheckPoint(request);
    }

    @Override
    public void ack(AckMessage request) {
        mqStore.ack(request);
    }

    @Override
    public AckResult changeInvisible(AckMessage request) {
        return mqStore.changeInvisible(request);
    }

    @Override
    public long getBufferedOffset(OffsetRequest request) {
        return mqStore.getBufferedOffset(request).getOffset();
    }

    @Override
    public QueueResult getMinOffset(QueueRequest request) {
        return mqStore.getMinOffset(request);
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        return mqStore.getMaxOffset(request);
    }

}
