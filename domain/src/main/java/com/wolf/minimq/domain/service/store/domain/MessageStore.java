package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.dto.MessageContainer;
import java.util.concurrent.CompletableFuture;

public interface MessageStore {
    EnqueueResult enqueue(MessageContainer context);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageContainer context);
}
