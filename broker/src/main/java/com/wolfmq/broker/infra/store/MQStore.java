package com.wolfmq.broker.infra.store;

import cn.coderule.common.util.lang.string.StringUtil;
import com.wolfmq.broker.infra.embed.EmbedMQStore;
import com.wolfmq.broker.infra.remote.RemoteMQStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import com.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import com.wolfmq.domain.domain.consumer.ack.store.CheckPointRequest;
import com.wolfmq.domain.domain.consumer.ack.store.OffsetRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import com.wolfmq.domain.domain.mq.dto.EnqueueRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.domain.mq.dto.DequeueRequest;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.rpc.store.facade.MQFacade;
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
        String topic = request.getTopicName();
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
        String topic = request.getTopicName();
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
        if (embedMQStore.containsTopic(request.getTopicName())) {
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
