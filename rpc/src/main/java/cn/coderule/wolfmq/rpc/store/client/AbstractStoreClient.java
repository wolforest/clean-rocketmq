package cn.coderule.wolfmq.rpc.store.client;

import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import cn.coderule.wolfmq.rpc.store.StoreClient;
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
