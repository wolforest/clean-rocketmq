package cn.coderule.minimq.domain.service.store.domain.mq;

import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import java.util.concurrent.CompletableFuture;

public interface MQService {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);

    GetResult dequeue(String topic, int queueId, int num);

    default GetResult get(String topic, int queueId) {
        return dequeue(topic, queueId, 1);
    }
}
