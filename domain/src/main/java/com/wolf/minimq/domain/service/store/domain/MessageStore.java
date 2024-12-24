package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.vo.EnqueueResult;
import com.wolf.minimq.domain.model.vo.MessageContext;
import java.util.concurrent.CompletableFuture;

public interface MessageStore {
    EnqueueResult enqueue(MessageContext context);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context);
}
