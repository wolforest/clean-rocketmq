package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.consumer.consume.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.DequeueResult;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MQStoreImpl implements MQStore {
    private final MQService mqService;

    public MQStoreImpl(MQService mqService) {
        this.mqService = mqService;
    }

    @Override
    public EnqueueResult enqueue(EnqueueRequest result) {
        return mqService.enqueue(result.getMessageBO());
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request) {
        return mqService.enqueueAsync(request.getMessageBO());
    }

    @Override
    public DequeueResult dequeue(DequeueRequest request) {
        return mqService.dequeue(request);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        return mqService.dequeueAsync(request);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
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
