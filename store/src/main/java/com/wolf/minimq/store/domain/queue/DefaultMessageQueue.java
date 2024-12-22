package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.context.MessageContext;
import com.wolf.minimq.domain.service.store.MessageQueue;
import com.wolf.minimq.domain.vo.EnqueueResult;
import java.util.concurrent.CompletableFuture;

public class DefaultMessageQueue implements MessageQueue {
    /**
     * enqueue single/multi message
     *  - assign consumeQueue offset
     *  - append commitLog
     *  - increase consumeQueue offset
     *
     * @param context messageContext
     * @return EnqueueResult
     */
    @Override
    public EnqueueResult enqueue(MessageContext context) {
        return null;
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context) {
        return null;
    }
}
