package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.dto.MessageContext;
import java.util.concurrent.CompletableFuture;

public interface MessageStore {
    EnqueueResult enqueue(MessageContext context);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context);
}
