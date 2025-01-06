package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import java.util.concurrent.CompletableFuture;

public interface MessageQueue {
    EnqueueResult enqueue(MessageBO messageBO);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO);
}
