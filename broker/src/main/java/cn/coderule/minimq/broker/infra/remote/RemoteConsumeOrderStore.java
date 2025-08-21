package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.facade.ConsumeOrderFacade;

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
