package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.model.dto.EnqueueResult;
import cn.coderule.minimq.domain.model.bo.MessageBO;
import cn.coderule.minimq.domain.model.dto.GetRequest;
import cn.coderule.minimq.domain.model.dto.GetResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Message pub/sub APIs
 */
public interface MessageStore {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);

    GetResult get(String topic, int queueId, long offset);
    GetResult get(String topic, int queueId, long offset, int num);
    GetResult get(GetRequest request);

    MessageBO getMessage(String topic, int queueId, long offset);
    List<MessageBO> getMessage(String topic, int queueId, long offset, int num);
}
