package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.client.MQClient;
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
        String topic = request.getTopic();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).dequeueAsync(request);
    }

    @Override
    public DequeueResult dequeue(DequeueRequest request) {
        String topic = request.getTopic();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).dequeue(request);
    }


    @Override
    public DequeueResult get(DequeueRequest request) {
        String address = loadBalance.findByTopic(request.getTopic());
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
    public long getBufferedOffset(OffsetRequest request) {
        String topic = request.getTopicName();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).getBufferedOffset(request);
    }

    @Override
    public QueueResult getMinOffset(QueueRequest request) {
        String topic = request.getTopic();
        String address = loadBalance.findByTopic(topic);
        return getClient(address).getMinOffset(request);
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        String topic = request.getTopic();
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
