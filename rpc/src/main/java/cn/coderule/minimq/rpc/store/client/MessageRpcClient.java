package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.model.bo.MessageBO;
import cn.coderule.minimq.domain.model.dto.EnqueueResult;
import cn.coderule.minimq.domain.model.dto.GetRequest;
import cn.coderule.minimq.domain.model.dto.GetResult;
import cn.coderule.minimq.domain.service.store.api.MessageService;
import cn.coderule.minimq.rpc.store.StoreClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageRpcClient extends AbstractStoreClient implements StoreClient, MessageService {
    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        if (isLocal(messageBO.getTopic())) {
            return localMessageService.enqueue(messageBO);
        }

        // rpcClient.invoke()
        return null;
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        if (isLocal(messageBO.getTopic())) {
            return localMessageService.enqueueAsync(messageBO);
        }

        return null;
    }

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        if (isLocal(topic)) {
            return localMessageService.get(topic, queueId, offset);
        }
        return null;
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        if (isLocal(topic)) {
            return localMessageService.get(topic, queueId, offset, num);
        }
        return null;
    }

    @Override
    public GetResult get(GetRequest request) {
        if (isLocal(request.getTopic())) {
            return localMessageService.get(request);
        }
        return null;
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        if (isLocal(topic)) {
            return localMessageService.getMessage(topic, queueId, offset);
        }
        return null;
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        if (isLocal(topic)) {
            return localMessageService.getMessage(topic, queueId, offset, num);
        }

        return List.of();
    }

}
