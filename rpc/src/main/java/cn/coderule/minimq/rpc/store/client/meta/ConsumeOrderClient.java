package cn.coderule.minimq.rpc.store.client.meta;

import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import cn.coderule.minimq.rpc.store.client.AbstractStoreClient;
import cn.coderule.minimq.rpc.store.facade.ConsumeOrderFacade;

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
