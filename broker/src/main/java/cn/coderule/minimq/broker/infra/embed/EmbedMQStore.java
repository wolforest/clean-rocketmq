package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.domain.service.store.domain.MQService;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EmbedMQStore extends AbstractEmbedStore implements MQStore {
    private final MQStore mqStore;
    public EmbedMQStore(MQStore mqStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.mqStore = mqStore;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return mqStore.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return mqStore.enqueueAsync(messageBO);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId, int num) {
        return mqStore.dequeueAsync(group, topic, queueId, num);
    }

    @Override
    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        return mqStore.dequeue(group, topic, queueId, num);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {
        return mqStore.get(topic, queueId, offset);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {
        return mqStore.get(topic, queueId, offset, num);
    }

    @Override
    public DequeueResult get(GetRequest request) {
        return mqStore.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        return mqStore.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        return mqStore.getMessage(topic, queueId, offset, num);
    }
}
