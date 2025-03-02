package cn.coderule.minimq.rpc.store;

import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.RegistryClient;

public interface StoreClient {
    void setRpcClient(RpcClient rpcClient);
    void setRegistryClient(RegistryClient registryClient);

    void setLocalMessageService(MessageStore messageStore);
    void setLocalTopicService(TopicStore topicStore);
    boolean isLocal(String topicName);
}
