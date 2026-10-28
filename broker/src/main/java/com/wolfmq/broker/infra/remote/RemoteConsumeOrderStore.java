package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.meta.order.OrderRequest;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.facade.ConsumeOrderFacade;

public class RemoteConsumeOrderStore extends AbstractRemoteStore implements ConsumeOrderFacade {

    private final BrokerConfig brokerConfig;
    private final RpcClient rpcClient;

    public RemoteConsumeOrderStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance, RpcClient rpcClient) {
        super(loadBalance);

        this.brokerConfig = brokerConfig;
        this.rpcClient = rpcClient;
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
