package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.MQService;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageStoreImpl implements MessageStore {
    private final MessageConfig messageConfig;
    private final MQService MQService;

    public MessageStoreImpl(MessageConfig messageConfig, MQService MQService) {
        this.messageConfig = messageConfig;
        this.MQService = MQService;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return MQService.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return MQService.enqueueAsync(messageBO);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {
        return MQService.get(topic, queueId, offset);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {
        return MQService.get(topic, queueId, offset, num);
    }

    @Override
    public DequeueResult get(GetRequest request) {
        return MQService.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        return MQService.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        return MQService.getMessage(topic, queueId, offset, num);
    }
}
