package com.wolf.minimq.store.api;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.service.store.api.MQService;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.service.store.domain.MessageQueue;
import com.wolf.minimq.store.domain.mq.DefaultMessageQueue;
import com.wolf.minimq.store.server.StoreContext;
import java.util.concurrent.CompletableFuture;

public class MQServiceImpl implements MQService {
    private final MessageConfig messageConfig;
    private final MessageQueue messageQueue;

    public MQServiceImpl(MessageConfig messageConfig, MessageQueue messageQueue) {
        this.messageConfig = messageConfig;
        this.messageQueue = messageQueue;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return messageQueue.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return messageQueue.enqueueAsync(messageBO);
    }
}
