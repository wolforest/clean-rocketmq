package cn.coderule.minimq.rpc.store;

import cn.coderule.minimq.domain.service.store.api.MessageService;
import cn.coderule.minimq.domain.service.store.api.TopicService;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.RegistryClient;

public interface StoreClient {
    void setRpcClient(RpcClient rpcClient);
    void setRegistryClient(RegistryClient registryClient);

    void setLocalMessageService(MessageService messageService);
    void setLocalTopicService(TopicService topicService);
    boolean isLocal(String topicName);
}
