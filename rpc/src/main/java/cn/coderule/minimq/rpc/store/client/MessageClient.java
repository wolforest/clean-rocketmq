package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.model.bo.MessageBO;
import cn.coderule.minimq.domain.model.dto.EnqueueResult;
import cn.coderule.minimq.domain.model.dto.GetRequest;
import cn.coderule.minimq.domain.model.dto.GetResult;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.rpc.store.StoreClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Setter;

@Setter
public class MessageClient extends AbstractStoreClient implements StoreClient, MessageStore {
    private MessageStore localMessageStore;

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        if (isLocal(messageBO.getTopic())) {
            return localMessageStore.enqueue(messageBO);
        }

        // rpcClient.invoke()
        return null;
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        if (isLocal(messageBO.getTopic())) {
            return localMessageStore.enqueueAsync(messageBO);
        }

        return null;
    }

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        if (isLocal(topic)) {
            return localMessageStore.get(topic, queueId, offset);
        }
        return null;
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        if (isLocal(topic)) {
            return localMessageStore.get(topic, queueId, offset, num);
        }
        return null;
    }

    @Override
    public GetResult get(GetRequest request) {
        if (isLocal(request.getTopic())) {
            return localMessageStore.get(request);
        }
        return null;
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        if (isLocal(topic)) {
            return localMessageStore.getMessage(topic, queueId, offset);
        }
        return null;
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        if (isLocal(topic)) {
            return localMessageStore.getMessage(topic, queueId, offset, num);
        }

        return List.of();
    }

}
