package com.wolf.minimq.domain.service.store;

import com.wolf.minimq.domain.vo.MessageContext;
import com.wolf.minimq.domain.vo.EnqueueResult;
import java.util.concurrent.CompletableFuture;

public interface MessageQueue {
    EnqueueResult enqueue(MessageContext context);
    CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context);


}
