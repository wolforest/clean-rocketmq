package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.DequeueRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.common.rpc.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.minimq.rpc.store.client.MQClient;
import java.util.List;
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
    public EnqueueResult enqueue(MessageBO messageBO) {
        String address = loadBalance.findByTopic(messageBO.getTopic());
        return getClient(address).enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        String address = loadBalance.findByTopic(messageBO.getTopic());
        return getClient(address).enqueueAsync(messageBO);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId, int num) {
        String address = loadBalance.findByTopic(topic);
        return getClient(address).dequeueAsync(group, topic, queueId, num);
    }

    @Override
    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        String address = loadBalance.findByTopic(topic);
        return getClient(address).dequeue(group, topic, queueId, num);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {
        String address = loadBalance.findByTopic(topic);
        return getClient(address).get(topic, queueId, offset);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {
        String address = loadBalance.findByTopic(topic);
        return getClient(address).get(topic, queueId, offset, num);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        String address = loadBalance.findByTopic(request.getTopic());
        return getClient(address).get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        String address = loadBalance.findByTopic(topic);
        return getClient(address).getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        String address = loadBalance.findByTopic(topic);
        return getClient(address).getMessage(topic, queueId, offset, num);
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
