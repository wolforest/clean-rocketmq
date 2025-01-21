package com.wolf.minimq.store.api;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.model.dto.GetRequest;
import com.wolf.minimq.domain.model.dto.GetResult;
import com.wolf.minimq.domain.service.store.api.MQService;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.service.store.domain.MessageQueue;
import com.wolf.minimq.store.domain.mq.DefaultMessageQueue;
import com.wolf.minimq.store.server.StoreContext;
import java.util.List;
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

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        return messageQueue.get(topic, queueId, offset);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        return messageQueue.get(topic, queueId, offset, num);
    }

    @Override
    public GetResult get(GetRequest request) {
        return messageQueue.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        return messageQueue.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        return messageQueue.getMessage(topic, queueId, offset, num);
    }
}
