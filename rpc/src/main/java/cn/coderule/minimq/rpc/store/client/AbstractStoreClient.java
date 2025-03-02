package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import lombok.Setter;

@Setter
public abstract class AbstractStoreClient implements StoreClient {
    protected MessageStore localMessageStore;
    protected TopicStore localTopicStore;
    protected RpcClient rpcClient;
    protected RegistryClient registryClient;

    @Override
    public boolean isLocal(String topicName) {
        return true;
    }
}
