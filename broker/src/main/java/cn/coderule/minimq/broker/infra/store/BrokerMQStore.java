package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedMQStore;
import cn.coderule.minimq.broker.infra.remote.RemoteMQStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.broker.infra.MQFacade;
import java.util.concurrent.CompletableFuture;

public class BrokerMQStore implements MQFacade {
    private final BrokerConfig brokerConfig;
    private final EmbedMQStore embedMQStore;
    private final RemoteMQStore remoteMQStore;

    public BrokerMQStore(BrokerConfig brokerConfig, EmbedMQStore embedMQStore, RemoteMQStore remoteMQStore) {
        this.brokerConfig = brokerConfig;
        this.embedMQStore = embedMQStore;
        this.remoteMQStore = remoteMQStore;
    }

    @Override
    public EnqueueResult enqueue(EnqueueRequest request) {
        MessageBO messageBO = request.getMessageBO();
        if (embedMQStore.isEmbed(messageBO.getTopic())) {
            return embedMQStore.enqueue(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return EnqueueResult.notAvailable();
        }

        return remoteMQStore.enqueue(request);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request) {
        MessageBO messageBO = request.getMessageBO();
        if (embedMQStore.isEmbed(messageBO.getTopic())) {
            return embedMQStore.enqueueAsync(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return CompletableFuture.completedFuture(
                EnqueueResult.notAvailable()
            );
        }

        return remoteMQStore.enqueueAsync(request);
    }


    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        String topic = request.getTopic();
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.dequeueAsync(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return CompletableFuture.completedFuture(
                DequeueResult.notFound()
            );
        }

        return remoteMQStore.dequeueAsync(request);
    }

    @Override
    public DequeueResult dequeue(DequeueRequest request) {
        String topic = request.getTopic();
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.dequeue(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return DequeueResult.notFound();
        }

        return remoteMQStore.dequeue(request);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        if (embedMQStore.isEmbed(request.getTopic())) {
            return embedMQStore.get(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return DequeueResult.notFound();
        }

        return remoteMQStore.get(request);
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {
        String topic = request.getCheckPoint().getTopic();
        if (embedMQStore.isEmbed(topic)) {
            embedMQStore.addCheckPoint(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteMQStore.addCheckPoint(request);
    }

    @Override
    public void ack(AckRequest request) {
        String topic = request.getAckMsg().getTopic();
        if (embedMQStore.isEmbed(topic)) {
            embedMQStore.ack(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteMQStore.ack(request);
    }

    @Override
    public long getBufferedOffset(OffsetRequest request) {
        String topic = request.getTopicName();
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.getBufferedOffset(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return 0;
        }
        return remoteMQStore.getBufferedOffset(request);
    }

    @Override
    public QueueResult getMinOffset(QueueRequest request) {
        String topic = request.getTopic();
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.getMinOffset(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return QueueResult.minOffset(0L);
        }

        return remoteMQStore.getMinOffset(request);
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        String topic = request.getTopic();
        if (embedMQStore.isEmbed(topic)) {
            return embedMQStore.getMaxOffset(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return QueueResult.maxOffset(0L);
        }

        return remoteMQStore.getMaxOffset(request);
    }

}
