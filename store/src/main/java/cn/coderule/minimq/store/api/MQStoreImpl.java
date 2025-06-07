package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MQStoreImpl implements MQStore {
    private final MessageConfig messageConfig;
    private final MQService mqService;

    public MQStoreImpl(MessageConfig messageConfig, MQService mqService) {
        this.messageConfig = messageConfig;
        this.mqService = mqService;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return mqService.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return mqService.enqueueAsync(messageBO);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId, int num) {
        return mqService.dequeueAsync(group, topic, queueId, num);
    }

    @Override
    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        return mqService.dequeue(group, topic, queueId, num);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {
        return mqService.get(topic, queueId, offset);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {
        return mqService.get(topic, queueId, offset, num);
    }

    @Override
    public DequeueResult get(GetRequest request) {
        return mqService.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        return mqService.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        return mqService.getMessage(topic, queueId, offset, num);
    }
}
