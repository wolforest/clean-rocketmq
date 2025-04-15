package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedMessageStore;
import cn.coderule.minimq.broker.infra.remote.RemoteMessageStore;
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
    private final EmbedMessageStore embedMessageStore;
    private final RemoteMessageStore remoteMessageStore;

    public BrokerMessageStore(BrokerConfig brokerConfig, EmbedMessageStore embedMessageStore, RemoteMessageStore remoteMessageStore) {
        this.brokerConfig = brokerConfig;
        this.embedMessageStore = embedMessageStore;
        this.remoteMessageStore = remoteMessageStore;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        if (embedMessageStore.isEmbed(messageBO.getTopic())) {
            return embedMessageStore.enqueue(messageBO);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return EnqueueResult.notAvailable();
        }

        return remoteMessageStore.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        if (embedMessageStore.isEmbed(messageBO.getTopic())) {
            return embedMessageStore.enqueueAsync(messageBO);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return CompletableFuture.completedFuture(EnqueueResult.notAvailable());
        }

        return remoteMessageStore.enqueueAsync(messageBO);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        if (embedMessageStore.isEmbed(topic)) {
            return embedMessageStore.get(topic, queueId, offset);
        }

        return remoteMessageStore.get(topic, queueId, offset);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        if (embedMessageStore.isEmbed(topic)) {
            return embedMessageStore.get(topic, queueId, offset, num);
        }

        return remoteMessageStore.get(topic, queueId, offset, num);
    }

    @Override
    public GetResult get(GetRequest request) {
        if (embedMessageStore.isEmbed(request.getTopic())) {
            return embedMessageStore.get(request);
        }

        return remoteMessageStore.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        if (embedMessageStore.isEmbed(topic)) {
            return embedMessageStore.getMessage(topic, queueId, offset);
        }

        return remoteMessageStore.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        if (embedMessageStore.isEmbed(topic)) {
            return embedMessageStore.getMessage(topic, queueId, offset, num);
        }

        return remoteMessageStore.getMessage(topic, queueId, offset, num);
    }
}
