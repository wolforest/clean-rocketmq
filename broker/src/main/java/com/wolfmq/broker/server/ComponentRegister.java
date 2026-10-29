package com.wolfmq.broker.server;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.util.lang.string.StringUtil;
import com.wolfmq.broker.domain.consumer.ConsumerBootstrap;
import com.wolfmq.broker.domain.producer.ProducerBootstrap;
import com.wolfmq.broker.domain.meta.MetaBootstrap;
import com.wolfmq.broker.domain.timer.TimerBootstrap;
import com.wolfmq.broker.domain.transaction.TransactionBootstrap;
import com.wolfmq.broker.infra.BrokerRegister;
import com.wolfmq.broker.infra.store.StoreBootstrap;
import com.wolfmq.broker.infra.task.TaskBootstrap;
import com.wolfmq.broker.server.channel.ChannelBootstrap;
import com.wolfmq.broker.server.grpc.GrpcBootstrap;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.rpc.common.rpc.netty.NettyClient;
import com.wolfmq.rpc.registry.route.RouteLoader;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();
    private final ApplicationContext context;
    private final BrokerConfig brokerConfig;

    public ComponentRegister(ApplicationContext context) {
        this.context = context;
        this.brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);
    }

    public LifecycleManager register() {
        return this.execute();
    }

    public LifecycleManager execute() {
        registerInfra();
        registerDomain();
        registerServer();

        return this.manager;
    }

    private void registerInfra() {
        registerNettyClient();
        registerBrokerRegister();

        registerStore();
        registerTask();
    }

    private void registerDomain() {
        registerRoute();

        registerTransaction();
        registerProducer();
        registerTimer();

        registerConsumer();
    }

    private void registerServer() {
        registerGrpc();
        registerRpc();
        registerChannel();
    }

    private void registerChannel() {
        ChannelBootstrap component = new ChannelBootstrap(context);
        manager.register(component);
    }

    private void registerGrpc() {
        GrpcBootstrap component = new GrpcBootstrap(context);
        manager.register(component);
    }

    private void registerRpc() {

    }

    private void registerNettyClient() {
        NettyClient component = new NettyClient(brokerConfig.getRpcClientConfig());
        context.register(component, ContextNamespace.APP);
    }

    private void registerBrokerRegister() {
        if (StringUtil.isBlank(brokerConfig.getRegistryAddress())) {
            return;
        }

        NettyClient nettyClient = context.getBean(NettyClient.class, ContextNamespace.APP);
        BrokerRegister component = new BrokerRegister(brokerConfig, nettyClient);
        manager.register(component);
        context.register(component, ContextNamespace.APP);

        BrokerRegister register = context.getBean(BrokerRegister.class, ContextNamespace.APP);
        RouteLoader loader = new RouteLoader(register.getRegistryClient());
        manager.register(loader);
    }

    private void registerTask() {
        TaskBootstrap component = new TaskBootstrap(context);
        manager.register(component);
    }

    private void registerStore() {
        StoreBootstrap component = new StoreBootstrap(context);
        manager.register(component);
    }

    private void registerRoute() {
        MetaBootstrap component = new MetaBootstrap(context);
        manager.register(component);
    }

    private void registerProducer() {
        ProducerBootstrap component = new ProducerBootstrap(context);
        manager.register(component);
    }

    private void registerConsumer() {
        ConsumerBootstrap component = new ConsumerBootstrap(context);
        manager.register(component);
    }

    private void registerTransaction() {
        TransactionBootstrap component = new TransactionBootstrap(context);
        manager.register(component);
    }

    private void registerTimer() {
        TimerBootstrap component = new TimerBootstrap(context);
        manager.register(component);
    }

}
