package cn.coderule.minimq.rpc.store;

import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.DefaultRegistryClient;

public interface StoreClient {
    void setRpcClient(RpcClient rpcClient);
    void setRegistryClient(DefaultRegistryClient registryClient);

    boolean isLocal(String topicName);
}
