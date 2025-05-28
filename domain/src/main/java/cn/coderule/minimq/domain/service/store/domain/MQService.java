package cn.coderule.minimq.domain.service.store.domain;

import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MQService {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);

    DequeueResult dequeue(String topic, int queueId, int num);
    default DequeueResult dequeue(String topic, int queueId) {
        return dequeue(topic, queueId, 1);
    }

    DequeueResult get(String topic, int queueId, long offset);
    DequeueResult get(String topic, int queueId, long offset, int num);
    DequeueResult get(GetRequest request);

    MessageBO getMessage(String topic, int queueId, long offset);
    List<MessageBO> getMessage(String topic, int queueId, long offset, int num);


}
