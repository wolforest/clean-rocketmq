package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.vo.EnqueueResult;
import com.wolf.minimq.domain.vo.MessageContext;
import java.util.concurrent.CompletableFuture;

public interface MessageStore {
    EnqueueResult enqueue(MessageContext context);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context);
}
