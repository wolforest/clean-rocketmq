package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.rpc.store.client.MessageClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RemoteMessageService implements MessageStore {
    private final BrokerConfig brokerConfig;
    private final MessageClient messageClient;

    public RemoteMessageService(BrokerConfig brokerConfig, MessageClient messageClient) {
        this.brokerConfig = brokerConfig;
        this.messageClient = messageClient;
    }

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
