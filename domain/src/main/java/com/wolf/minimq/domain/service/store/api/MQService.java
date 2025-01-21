package com.wolf.minimq.domain.service.store.api;

import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.model.dto.GetRequest;
import com.wolf.minimq.domain.model.dto.GetResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Message pub/sub APIs
 */
public interface MQService {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);

    GetResult get(String topic, int queueId, long offset);
    GetResult get(String topic, int queueId, long offset, int num);
    GetResult get(GetRequest request);

    MessageBO getMessage(String topic, int queueId, long offset);
    List<MessageBO> getMessage(String topic, int queueId, long offset, int num);
}
