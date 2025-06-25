package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.domain.consumer.consume.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import java.util.List;
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
    MessageBO getMessage(String topic, int queueId, long offset);
    List<MessageBO> getMessage(String topic, int queueId, long offset, int num);
}
