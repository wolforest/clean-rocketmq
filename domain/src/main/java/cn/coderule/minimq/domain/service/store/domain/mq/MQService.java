package cn.coderule.minimq.domain.service.store.domain.mq;

import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.consumer.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.DequeueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MQService {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);

    CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId, int num);

    default CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId) {
        return dequeueAsync(group, topic, queueId, 1);
    }

    DequeueResult dequeue(String group, String topic, int queueId, int num);
    default DequeueResult dequeue(String group, String topic, int queueId) {
        return dequeue(group, topic, queueId, 1);
    }

    DequeueResult get(String topic, int queueId, long offset);
    DequeueResult get(String topic, int queueId, long offset, int num);
    DequeueResult get(DequeueRequest request);

    MessageBO getMessage(String topic, int queueId, long offset);
    List<MessageBO> getMessage(String topic, int queueId, long offset, int num);


}
