package com.wolf.minimq.domain.service.store.api;

import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.dto.MessageContext;
import java.util.concurrent.CompletableFuture;

/**
 * Message pub/sub APIs
 */
public interface MessageService {
    EnqueueResult enqueue(MessageContext context);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context);
}
