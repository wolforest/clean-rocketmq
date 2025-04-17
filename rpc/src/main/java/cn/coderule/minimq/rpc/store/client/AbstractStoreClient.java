package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.rpc.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import lombok.Getter;

@Getter
public abstract class AbstractStoreClient implements StoreClient {
    protected final String address;
    protected final RpcClient rpcClient;

    public AbstractStoreClient(RpcClient rpcClient, String address) {
        this.rpcClient = rpcClient;
        this.address = address;
    }
}
