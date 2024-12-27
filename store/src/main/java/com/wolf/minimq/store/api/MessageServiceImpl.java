package com.wolf.minimq.store.api;

import com.wolf.minimq.domain.service.store.api.MessageService;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.dto.MessageContext;
import com.wolf.minimq.store.domain.message.DefaultMessageStore;
import com.wolf.minimq.store.server.StoreContext;
import java.util.concurrent.CompletableFuture;

public class MessageServiceImpl implements MessageService {
    @Override
    public EnqueueResult enqueue(MessageContext context) {
        return StoreContext.getBean(DefaultMessageStore.class).enqueue(context);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageContext context) {
        return StoreContext.getBean(DefaultMessageStore.class).enqueueAsync(context);
    }
}
