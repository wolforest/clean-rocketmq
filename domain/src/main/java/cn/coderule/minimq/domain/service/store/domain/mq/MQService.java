package cn.coderule.minimq.domain.service.store.domain.mq;

import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MQService {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);

    DequeueResult dequeue(DequeueRequest request);
    CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request);

    DequeueResult get(String topic, int queueId, long offset);
    DequeueResult get(DequeueRequest request);

    MessageBO getMessage(String topic, int queueId, long offset);
    List<MessageBO> getMessage(String topic, int queueId, long offset, int num);
}
