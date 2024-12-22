package com.wolf.minimq.store.api;

import com.wolf.minimq.domain.service.store.api.MessageQueueService;
import com.wolf.minimq.domain.vo.EnqueueResult;
import com.wolf.minimq.domain.vo.MessageContext;
import com.wolf.minimq.store.domain.queue.MessageQueueDomainService;
import com.wolf.minimq.store.server.StoreContext;
import java.util.concurrent.CompletableFuture;

public class MessageQueueServiceImpl implements MessageQueueService {
    @Override
    public EnqueueResult enqueue(MessageContext context) {
        return StoreContext.getBean(MessageQueueDomainService.class).enqueue(context);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context) {
        return StoreContext.getBean(MessageQueueDomainService.class).enqueueAsync(context);
    }
}
