package com.wolfmq.rpc.store.client.meta;

import com.wolfmq.domain.domain.meta.order.OrderRequest;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.StoreClient;
import com.wolfmq.rpc.store.client.AbstractStoreClient;
import com.wolfmq.rpc.store.facade.ConsumeOrderFacade;

public class ConsumeOrderClient extends AbstractStoreClient implements StoreClient, ConsumeOrderFacade {
    public ConsumeOrderClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public boolean isLocked(OrderRequest request) {
        return false;
    }

    @Override
    public void lock(OrderRequest request) {

    }

    @Override
    public void unlock(OrderRequest request) {

    }

    @Override
    public long commit(OrderRequest request) {
        return 0;
    }

    @Override
    public void updateInvisible(OrderRequest request) {

    }
}
