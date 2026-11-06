package com.wolfmq.rpc.store.facade;

import com.wolfmq.common.domain.consumer.ack.broker.AckResult;
import com.wolfmq.common.domain.consumer.ack.store.AckMessage;
import com.wolfmq.common.domain.consumer.ack.store.CheckPointRequest;
import com.wolfmq.common.domain.consumer.ack.store.OffsetRequest;
import com.wolfmq.common.domain.messagequeue.dto.DequeueRequest;
import com.wolfmq.common.domain.messagequeue.dto.DequeueResult;
import com.wolfmq.common.domain.consumer.consume.dto.MessageRequest;
import com.wolfmq.common.domain.consumer.consume.dto.MessageResult;
import com.wolfmq.common.domain.consumer.consume.dto.QueueRequest;
import com.wolfmq.common.domain.consumer.consume.dto.QueueResult;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueRequest;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueResult;
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
