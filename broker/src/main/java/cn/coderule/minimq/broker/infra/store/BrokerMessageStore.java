package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedMessageService;
import cn.coderule.minimq.broker.infra.remote.RemoteMessageService;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BrokerMessageStore implements MessageStore {
    private final BrokerConfig brokerConfig;
    private final EmbedMessageService embedMessageService;
    private final RemoteMessageService remoteMessageService;

    public BrokerMessageStore(BrokerConfig brokerConfig, EmbedMessageService embedMessageService, RemoteMessageService remoteMessageService) {
        this.brokerConfig = brokerConfig;
        this.embedMessageService = embedMessageService;
        this.remoteMessageService = remoteMessageService;
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
