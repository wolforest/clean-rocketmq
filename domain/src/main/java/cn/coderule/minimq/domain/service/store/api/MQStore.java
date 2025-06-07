package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.DequeueRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Message pub/sub APIs
 */
public interface MQStore {
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
