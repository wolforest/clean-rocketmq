package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import com.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import com.wolfmq.domain.domain.consumer.ack.store.CheckPointRequest;
import com.wolfmq.domain.domain.consumer.ack.store.OffsetRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.mq.dto.EnqueueRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.domain.mq.dto.DequeueRequest;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.client.MQClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteMQStore extends AbstractRemoteStore implements MQFacade {
    private final BrokerConfig brokerConfig;
    private final ConcurrentMap<String, MQClient> clientMap;
    private final RpcClient rpcClient;

    public RemoteMQStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance, RpcClient rpcClient) {
        super(loadBalance);

        this.brokerConfig = brokerConfig;
        clientMap = new ConcurrentHashMap<>();
        this.rpcClient = rpcClient;
    }

    @Override
    public EnqueueResult enqueue(EnqueueRequest request) {
        MessageBO messageBO = request.getMessageBO();
        String address = loadBalance.findByTopic(messageBO.getTopic());
        return getClient(address).enqueue(request);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request) {
        MessageBO messageBO = request.getMessageBO();
        String address = loadBalance.findByTopic(messageBO.getTopic());
        return getClient(address).enqueueAsync(request);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        String topic = request.getTopicName();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).dequeueAsync(request);
    }

    @Override
    public DequeueResult dequeue(DequeueRequest request) {
        String topic = request.getTopicName();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).dequeue(request);
    }


    @Override
    public DequeueResult get(DequeueRequest request) {
        String address = loadBalance.findByTopic(request.getTopicName());
        return getClient(address).get(request);
    }

    @Override
    public MessageResult getMessage(MessageRequest request) {
        String address = loadBalance.findByStoreGroup(request.getStoreGroup());
        return getClient(address).getMessage(request);
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {
        String topic = request.getCheckPoint().getTopic();
        String address = loadBalance.findByTopic(topic);
        getClient(address).addCheckPoint(request);
    }

    @Override
    public void ack(AckMessage request) {
        String topic = request.getAckInfo().getTopic();
        String address = loadBalance.findByTopic(topic);
        getClient(address).ack(request);
    }

    @Override
    public AckResult changeInvisible(AckMessage request) {
        String topic = request.getAckInfo().getTopic();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).changeInvisible(request);
    }

    @Override
    public long getBufferedOffset(OffsetRequest request) {
        String topic = request.getTopicName();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).getBufferedOffset(request);
    }

    @Override
    public QueueResult getMinOffset(QueueRequest request) {
        String topic = request.getTopicName();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).getMinOffset(request);
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        String topic = request.getTopicName();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).getMaxOffset(request);
    }

    public MQClient getClient(String address) {
        if (clientMap.containsKey(address)) {
            return clientMap.get(address);
        }

        MQClient client = new MQClient(this.rpcClient, address);
        MQClient prev = clientMap.putIfAbsent(address, client);

        return prev == null ? client : prev;
    }


}
