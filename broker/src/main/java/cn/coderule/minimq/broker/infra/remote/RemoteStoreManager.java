package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.rpc.registry.client.RouteLoader;
import cn.coderule.minimq.broker.server.BrokerContext;
import cn.coderule.minimq.domain.config.BrokerConfig;

public class RemoteStoreManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private RemoteLoadBalance loadBalance;

    @Override
    public void initialize() {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        if (!this.brokerConfig.isEnableRemoteStore()) {
            return;
        }

        RouteLoader routeLoader = BrokerContext.getBean(RouteLoader.class);
        this.loadBalance = new RemoteLoadBalance(this.brokerConfig, routeLoader);


    }

    @Override
    public void start() {
        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }
    }

    @Override
    public void shutdown() {
        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }
    }

}
