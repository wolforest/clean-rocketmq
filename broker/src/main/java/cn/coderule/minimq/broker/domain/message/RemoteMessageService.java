package cn.coderule.minimq.broker.domain.message;

import cn.coderule.minimq.domain.model.bo.MessageBO;
import cn.coderule.minimq.domain.model.dto.EnqueueResult;
import cn.coderule.minimq.domain.model.dto.GetRequest;
import cn.coderule.minimq.domain.model.dto.GetResult;
import cn.coderule.minimq.domain.service.store.domain.MessageStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RemoteMessageService implements MessageStore {
    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return null;
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return null;
    }

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        return null;
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        return null;
    }

    @Override
    public GetResult get(GetRequest request) {
        return null;
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        return null;
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        return List.of();
    }
}
