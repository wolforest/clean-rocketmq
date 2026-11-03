package com.wolfmq.common.server.store.api;

import com.wolfmq.common.domain.consumer.ack.broker.AckResult;
import com.wolfmq.common.domain.consumer.ack.store.AckMessage;
import com.wolfmq.common.domain.consumer.ack.store.CheckPointRequest;
import com.wolfmq.common.domain.consumer.ack.store.OffsetRequest;
import com.wolfmq.common.domain.mq.dto.DequeueRequest;
import com.wolfmq.common.domain.mq.dto.DequeueResult;
import com.wolfmq.common.domain.consumer.consume.dto.MessageRequest;
import com.wolfmq.common.domain.consumer.consume.dto.MessageResult;
import com.wolfmq.common.domain.consumer.consume.dto.QueueRequest;
import com.wolfmq.common.domain.consumer.consume.dto.QueueResult;
import com.wolfmq.common.domain.mq.dto.EnqueueRequest;
import com.wolfmq.common.domain.mq.dto.EnqueueResult;
import java.util.concurrent.CompletableFuture;

/**
 * Message pub/sub APIs
 */
public interface MQStore {
    EnqueueResult enqueue(EnqueueRequest request);
    CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request);

    DequeueResult dequeue(DequeueRequest request);
    CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request);

    DequeueResult get(DequeueRequest request);
    MessageResult getMessage(MessageRequest request);

    void addCheckPoint(CheckPointRequest request);
    void ack(AckMessage request);
    AckResult changeInvisible(AckMessage request);


    QueueResult getBufferedOffset(OffsetRequest request);
    QueueResult getMinOffset(QueueRequest request);
    QueueResult getMaxOffset(QueueRequest request);
}
