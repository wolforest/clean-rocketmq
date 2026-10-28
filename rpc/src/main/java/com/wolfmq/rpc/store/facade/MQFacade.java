package com.wolfmq.rpc.store.facade;

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
import java.util.concurrent.CompletableFuture;

/**
 * RPC facade, switch storage between
 *  - embed storage
 *  - remote storage
 */
public interface MQFacade {
    EnqueueResult enqueue(EnqueueRequest request);
    CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request);

    DequeueResult dequeue(DequeueRequest request);
    CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request);

    DequeueResult get(DequeueRequest request);
    MessageResult getMessage(MessageRequest request);

    void addCheckPoint(CheckPointRequest request);
    void ack(AckMessage request);
    AckResult changeInvisible(AckMessage request);
    long getBufferedOffset(OffsetRequest request);

    QueueResult getMinOffset(QueueRequest request);
    QueueResult getMaxOffset(QueueRequest request);
}
