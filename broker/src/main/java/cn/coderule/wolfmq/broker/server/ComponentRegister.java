package cn.coderule.wolfmq.broker.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.wolfmq.broker.domain.consumer.ConsumerBootstrap;
import cn.coderule.wolfmq.broker.domain.producer.ProducerBootstrap;
import cn.coderule.wolfmq.broker.domain.meta.MetaBootstrap;
import cn.coderule.wolfmq.broker.domain.timer.TimerBootstrap;
import cn.coderule.wolfmq.broker.domain.transaction.TransactionBootstrap;
import cn.coderule.wolfmq.broker.infra.BrokerRegister;
import cn.coderule.wolfmq.broker.infra.store.StoreBootstrap;
import cn.coderule.wolfmq.broker.infra.task.TaskBootstrap;
import cn.coderule.wolfmq.broker.server.bootstrap.BrokerContext;
import cn.coderule.wolfmq.broker.server.grpc.GrpcBootstrap;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.wolfmq.rpc.registry.route.RouteLoader;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();
    private final BrokerConfig brokerConfig;

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        StoreContext.register(register);

        return register.execute();
    }

    public ComponentRegister() {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);
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
    }

    private void registerGrpc() {
        GrpcBootstrap component = new GrpcBootstrap();
        manager.register(component);
    }

    private void registerRpc() {

    }

    private void registerNettyClient() {
        NettyClient component = new NettyClient(brokerConfig.getRpcClientConfig());
        BrokerContext.register(component);
    }

    private void registerBrokerRegister() {
        if (StringUtil.isBlank(brokerConfig.getRegistryAddress())) {
            return;
        }

        NettyClient nettyClient = BrokerContext.getBean(NettyClient.class);
        BrokerRegister component = new BrokerRegister(brokerConfig, nettyClient);
        manager.register(component);
        BrokerContext.register(component);

        BrokerRegister register = BrokerContext.getBean(BrokerRegister.class);
        RouteLoader loader = new RouteLoader(register.getRegistryClient());
        manager.register(loader);
    }

    private void registerTask() {
        TaskBootstrap component = new TaskBootstrap();
        manager.register(component);
    }

    private void registerStore() {
        StoreBootstrap component = new StoreBootstrap();
        manager.register(component);
    }

    private void registerRoute() {
        MetaBootstrap component = new MetaBootstrap();
        manager.register(component);
    }

    private void registerProducer() {
        ProducerBootstrap component = new ProducerBootstrap();
        manager.register(component);
    }

    private void registerConsumer() {
        ConsumerBootstrap component = new ConsumerBootstrap();
        manager.register(component);
    }

    private void registerTransaction() {
        TransactionBootstrap component = new TransactionBootstrap();
        manager.register(component);
    }

    private void registerTimer() {
        TimerBootstrap component = new TimerBootstrap();
        manager.register(component);
    }

}
