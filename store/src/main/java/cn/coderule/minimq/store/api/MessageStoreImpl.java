package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.model.dto.GetRequest;
import cn.coderule.minimq.domain.model.dto.GetResult;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.model.dto.EnqueueResult;
import cn.coderule.minimq.domain.model.bo.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.MessageQueue;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageStoreImpl implements MessageStore {
    private final MessageConfig messageConfig;
    private final MessageQueue messageQueue;

    public MessageStoreImpl(MessageConfig messageConfig, MessageQueue messageQueue) {
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
