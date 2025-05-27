package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.mq.MessageService;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageStoreImpl implements MessageStore {
    private final MessageConfig messageConfig;
    private final MessageService messageService;

    public MessageStoreImpl(MessageConfig messageConfig, MessageService messageService) {
        this.messageConfig = messageConfig;
        this.messageService = messageService;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return messageService.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return messageService.enqueueAsync(messageBO);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        return messageService.get(topic, queueId, offset);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        return messageService.get(topic, queueId, offset, num);
    }

    @Override
    public GetResult get(GetRequest request) {
        return messageService.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        return messageService.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        return messageService.getMessage(topic, queueId, offset, num);
    }
}
