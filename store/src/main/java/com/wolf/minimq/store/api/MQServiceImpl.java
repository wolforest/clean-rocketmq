package com.wolf.minimq.store.api;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.model.dto.GetRequest;
import com.wolf.minimq.domain.model.dto.GetResult;
import com.wolf.minimq.domain.service.store.api.MQService;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.service.store.domain.MessageStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MQServiceImpl implements MQService {
    private final MessageConfig messageConfig;
    private final MessageStore messageStore;

    public MQServiceImpl(MessageConfig messageConfig, MessageStore messageStore) {
        this.messageConfig = messageConfig;
        this.messageStore = messageStore;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return messageStore.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return messageStore.enqueueAsync(messageBO);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        return messageStore.get(topic, queueId, offset);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        return messageStore.get(topic, queueId, offset, num);
    }

    @Override
    public GetResult get(GetRequest request) {
        return messageStore.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        return messageStore.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        return messageStore.getMessage(topic, queueId, offset, num);
    }
}
