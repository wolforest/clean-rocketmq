package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.netty.NettyClient;
import cn.coderule.minimq.rpc.store.client.MessageClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteMessageStore extends AbstractRemoteStore implements MessageStore {
    private final ConcurrentMap<String, MessageClient> clientMap;
    private final RpcClient rpcClient;

    public RemoteMessageStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance) {
        super(loadBalance);

        clientMap = new ConcurrentHashMap<>();
        this.rpcClient = new NettyClient(new RpcClientConfig());
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
    public GetResult get(String topic, int queueId, long offset) {
        String address = loadBalance.findByTopic(topic);
        return getClient(address).get(topic, queueId, offset);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        String address = loadBalance.findByTopic(topic);
        return getClient(address).get(topic, queueId, offset, num);
    }

    @Override
    public GetResult get(GetRequest request) {
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

    private MessageClient getClient(String address) {
        if (clientMap.containsKey(address)) {
            return clientMap.get(address);
        }

        MessageClient client = new MessageClient(this.rpcClient, address);
        MessageClient prev = clientMap.putIfAbsent(address, client);

        return prev == null ? client : prev;
    }
}
