package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.model.dto.GetResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageQueue {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);

    /**
     * get message from queue, this will not change the consume offset
     * @param topic topic
     * @param queueId queueId
     * @param offset offset
     * @return MessageBO
     */
    GetResult get(String topic, int queueId, long offset);

    /**
     * fetch message from queue, this will not change the consume offset
     * @param topic topic
     * @param queueId queueId
     * @param offset offset
     * @param num num
     * @return MessageBO
     */
    GetResult get(String topic, int queueId, long offset, int num);
}
