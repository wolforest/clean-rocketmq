package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedMQStore;
import cn.coderule.minimq.broker.infra.remote.RemoteMQStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.consumer.DequeueRequest;
import cn.coderule.minimq.domain.domain.model.consumer.DequeueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BrokerMQStore implements MQStore {
    private final BrokerConfig brokerConfig;
    private final EmbedMQStore embedMQStore;
    private final RemoteMQStore remoteMQStore;

    public BrokerMQStore(BrokerConfig brokerConfig, EmbedMQStore embedMQStore, RemoteMQStore remoteMQStore) {
        this.brokerConfig = brokerConfig;
        this.embedMQStore = embedMQStore;
        this.remoteMQStore = remoteMQStore;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        if (embedMQStore.isEmbed(messageBO.getTopic())) {
            return embedMQStore.enqueue(messageBO);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return EnqueueResult.notAvailable();
        }

        return remoteMQStore.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        if (embedMQStore.isEmbed(messageBO.getTopic())) {
            return embedMQStore.enqueueAsync(messageBO);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return CompletableFuture.completedFuture(EnqueueResult.notAvailable());
        }

        return remoteMQStore.enqueueAsync(messageBO);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId, int num) {
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.dequeueAsync(group, topic, queueId, num);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return CompletableFuture.completedFuture(DequeueResult.notFound());
        }

        return remoteMQStore.dequeueAsync(group, topic, queueId, num);
    }

    @Override
    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.dequeue(group, topic, queueId, num);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return DequeueResult.notFound();
        }

        return remoteMQStore.dequeue(group, topic, queueId, num);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.get(topic, queueId, offset);
        }

        return remoteMQStore.get(topic, queueId, offset);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.get(topic, queueId, offset, num);
        }

        return remoteMQStore.get(topic, queueId, offset, num);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        if (embedMQStore.isEmbed(request.getTopic())) {
            return embedMQStore.get(request);
        }

        return remoteMQStore.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.getMessage(topic, queueId, offset);
        }

        return remoteMQStore.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.getMessage(topic, queueId, offset, num);
        }

        return remoteMQStore.getMessage(topic, queueId, offset, num);
    }
}
