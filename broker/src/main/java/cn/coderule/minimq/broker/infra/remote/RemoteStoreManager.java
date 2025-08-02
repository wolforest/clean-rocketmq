package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.minimq.rpc.registry.route.RouteLoader;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class RemoteStoreManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private RemoteLoadBalance loadBalance;
    private RpcClient rpcClient;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        if (!this.brokerConfig.isEnableRemoteStore()) {
            return;
        }

        RouteLoader routeLoader = BrokerContext.getBean(RouteLoader.class);
        this.loadBalance = new RemoteLoadBalance(this.brokerConfig, routeLoader);
        this.rpcClient = BrokerContext.getBean(NettyClient.class);

        initServices();
    }

    @Override
    public void start() throws Exception {
        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }
    }

    private void initServices() {
        RemoteMQStore mqStore = new RemoteMQStore(brokerConfig, loadBalance, rpcClient);
        BrokerContext.register(mqStore);

        RemoteTopicStore topicStore = new RemoteTopicStore(brokerConfig, loadBalance, rpcClient);
        BrokerContext.register(topicStore);

        RemoteSubscriptionStore subscriptionStore = new RemoteSubscriptionStore(brokerConfig, loadBalance, rpcClient);
        BrokerContext.register(subscriptionStore);

        RemoteConsumeOffsetStore consumeOffsetStore = new RemoteConsumeOffsetStore(brokerConfig, loadBalance, rpcClient);
        BrokerContext.register(consumeOffsetStore);

        RemoteTimerStore timerStore = new RemoteTimerStore(brokerConfig, loadBalance, rpcClient);
        BrokerContext.register(timerStore);

    }

}
