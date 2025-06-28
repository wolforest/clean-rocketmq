package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.service.broker.infra.MQStore;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.common.rpc.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.minimq.rpc.store.client.MQClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteMQStore extends AbstractRemoteStore implements MQStore, Lifecycle {
    private final BrokerConfig brokerConfig;
    private final ConcurrentMap<String, MQClient> clientMap;
    private final RpcClient rpcClient;

    public RemoteMQStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance) {
        super(loadBalance);

        this.brokerConfig = brokerConfig;
        clientMap = new ConcurrentHashMap<>();
        this.rpcClient = new NettyClient(new RpcClientConfig());
    }

    @Override
    public void start() {
        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        rpcClient.start();
    }

    @Override
    public void shutdown() {
        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        rpcClient.shutdown();
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

    public MQClient getClient(String address) {
        if (clientMap.containsKey(address)) {
            return clientMap.get(address);
        }

        MQClient client = new MQClient(this.rpcClient, address);
        MQClient prev = clientMap.putIfAbsent(address, client);

        return prev == null ? client : prev;
    }


}
