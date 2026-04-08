package cn.coderule.wolfmq.domain.domain.store.api;

import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
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
