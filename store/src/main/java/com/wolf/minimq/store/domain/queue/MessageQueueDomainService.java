package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.vo.MessageContext;
import com.wolf.minimq.domain.vo.EnqueueResult;
import java.util.concurrent.CompletableFuture;

public class MessageQueueDomainService {
    /**
     * enqueue single/multi message
     *  - assign consumeQueue offset
     *  - append commitLog
     *  - increase consumeQueue offset
     *
     * @param context messageContext
     * @return EnqueueResult
     */
    public EnqueueResult enqueue(MessageContext context) {
        return null;
    }

    public CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context) {
        return null;
    }
}
