package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.service.store.domain.mq.MessageService;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EmbedMessageStore extends AbstractEmbedStore implements MessageService {
    private final MessageStore messageStore;
    public EmbedMessageStore(MessageStore messageStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
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
    public DequeueResult get(String topic, int queueId, long offset) {
        return messageStore.get(topic, queueId, offset);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {
        return messageStore.get(topic, queueId, offset, num);
    }

    @Override
    public DequeueResult get(GetRequest request) {
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
