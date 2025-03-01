package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.service.store.api.MessageService;
import cn.coderule.minimq.domain.service.store.api.TopicService;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import lombok.Setter;

@Setter
public abstract class AbstractStoreClient implements StoreClient {
    protected MessageService localMessageService;
    protected TopicService localTopicService;
    protected RpcClient rpcClient;
    protected RegistryClient registryClient;

    @Override
    public boolean isLocal(String topicName) {
        return true;
    }
}
