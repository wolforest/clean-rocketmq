package com.wolfmq.rpc.store.client;

import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.StoreClient;
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
