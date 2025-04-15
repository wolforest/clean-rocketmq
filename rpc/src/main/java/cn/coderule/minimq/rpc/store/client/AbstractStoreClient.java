package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.client.DefaultRegistryClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class AbstractStoreClient implements StoreClient {
    protected final String address;
    protected final RpcClient rpcClient;

    public AbstractStoreClient(RpcClient rpcClient, String address) {
        this.rpcClient = rpcClient;
        this.address = address;
    }
}
