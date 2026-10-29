package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.common.rpc.netty.NettyClient;
import com.wolfmq.rpc.registry.route.RouteLoader;
import com.wolfmq.domain.config.server.BrokerConfig;

public class RemoteStoreBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public RemoteStoreBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;
    private RemoteLoadBalance loadBalance;
    private RpcClient rpcClient;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);
        if (!this.brokerConfig.isEnableRemoteStore()) {
            return;
        }

        RouteLoader routeLoader = context.getBean(RouteLoader.class, ContextNamespace.APP);
        this.loadBalance = new RemoteLoadBalance(this.brokerConfig, routeLoader);
        this.rpcClient = context.getBean(NettyClient.class, ContextNamespace.APP);

        initServices();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
    }

    private void initServices() {
        RemoteMQStore mqStore =
            new RemoteMQStore(brokerConfig, loadBalance, rpcClient);
        context.register(mqStore, ContextNamespace.APP);

        RemoteTopicStore topicStore =
            new RemoteTopicStore(brokerConfig, loadBalance, rpcClient);
        context.register(topicStore, ContextNamespace.APP);

        RemoteSubscriptionStore subscriptionStore =
            new RemoteSubscriptionStore(brokerConfig, loadBalance, rpcClient);
        context.register(subscriptionStore, ContextNamespace.APP);

        RemoteConsumeOffsetStore consumeOffsetStore =
            new RemoteConsumeOffsetStore(brokerConfig, loadBalance, rpcClient);
        context.register(consumeOffsetStore, ContextNamespace.APP);

        RemoteTimerStore timerStore =
            new RemoteTimerStore(brokerConfig, loadBalance, rpcClient);
        context.register(timerStore, ContextNamespace.APP);

    }

}
