package cn.coderule.minimq.broker.infra.store;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.infra.embed.EmbedMQStore;
import cn.coderule.minimq.broker.infra.remote.RemoteMQStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;

public class MQStore implements MQFacade {
    private final BrokerConfig brokerConfig;
    private final EmbedMQStore embedMQStore;
    private final RemoteMQStore remoteMQStore;

    public MQStore(BrokerConfig brokerConfig, EmbedMQStore embedMQStore, RemoteMQStore remoteMQStore) {
        this.brokerConfig = brokerConfig;
        this.embedMQStore = embedMQStore;
        this.remoteMQStore = remoteMQStore;
    }

    @Override
    public EnqueueResult enqueue(EnqueueRequest request) {
        MessageBO messageBO = request.getMessageBO();
        if (embedMQStore.containsTopic(messageBO.getTopic())) {
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
        if (embedMQStore.containsTopic(messageBO.getTopic())) {
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
        if (embedMQStore.containsTopic(topic)) {
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
        if (embedMQStore.containsTopic(topic)) {
            return embedMQStore.dequeue(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return DequeueResult.notFound();
        }

        return remoteMQStore.dequeue(request);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        if (embedMQStore.containsTopic(request.getTopic())) {
            return embedMQStore.get(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return DequeueResult.notFound();
        }

        return remoteMQStore.get(request);
    }

    @Override
    public MessageResult getMessage(MessageRequest request) {
        if (StringUtil.isBlank(request.getStoreGroup())) {
            return embedMQStore.getMessage(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return MessageResult.notFound();
        }

        return embedMQStore.getMessage(request);
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {
        String topic = request.getCheckPoint().getTopic();
        if (embedMQStore.containsTopic(topic)) {
            embedMQStore.addCheckPoint(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteMQStore.addCheckPoint(request);
    }

    @Override
    public void ack(AckMessage request) {
        String topic = request.getAckInfo().getTopic();
        if (embedMQStore.containsTopic(topic)) {
            embedMQStore.ack(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteMQStore.ack(request);
    }

    @Override
    public AckResult changeInvisible(AckMessage request) {
        String topic = request.getAckInfo().getTopic();
        if (embedMQStore.containsTopic(topic)) {
            return embedMQStore.changeInvisible(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return AckResult.failure();
        }

        return remoteMQStore.changeInvisible(request);
    }

    @Override
    public long getBufferedOffset(OffsetRequest request) {
        String topic = request.getTopicName();
        if (embedMQStore.containsTopic(topic)) {
            return embedMQStore.getBufferedOffset(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return 0;
        }
        return remoteMQStore.getBufferedOffset(request);
    }

    @Override
    public QueueResult getMinOffset(QueueRequest request) {
        String topic = request.getTopicName();
        if (embedMQStore.containsTopic(topic)) {
            return embedMQStore.getMinOffset(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return QueueResult.minOffset(0L);
        }

        return remoteMQStore.getMinOffset(request);
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        String topic = request.getTopicName();
        if (embedMQStore.containsTopic(topic)) {
            return embedMQStore.getMaxOffset(request);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return QueueResult.maxOffset(0L);
        }

        return remoteMQStore.getMaxOffset(request);
    }

}
