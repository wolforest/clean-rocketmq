package com.wolf.minimq.store.api;

import com.wolf.minimq.domain.service.store.api.MQService;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.store.domain.mq.DefaultMessageQueue;
import com.wolf.minimq.store.server.StoreContext;
import java.util.concurrent.CompletableFuture;

public class MQServiceImpl implements MQService {
    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return StoreContext.getBean(DefaultMessageQueue.class).enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return StoreContext.getBean(DefaultMessageQueue.class).enqueueAsync(messageBO);
    }
}
