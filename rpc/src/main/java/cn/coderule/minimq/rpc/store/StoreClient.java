package cn.coderule.minimq.rpc.store;

import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.RegistryClient;

public interface StoreClient {
    void setRpcClient(RpcClient rpcClient);
    void setRegistryClient(RegistryClient registryClient);

    boolean isLocal(String topicName);
}
