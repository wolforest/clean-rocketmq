package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.DefaultRegistryClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import lombok.Setter;

@Setter
public class AbstractStoreClient implements StoreClient {
    protected RpcClient rpcClient;
    protected DefaultRegistryClient registryClient;

    @Override
    public boolean isLocal(String topicName) {
        return true;
    }
}
