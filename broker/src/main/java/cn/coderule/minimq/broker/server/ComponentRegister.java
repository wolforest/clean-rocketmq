package cn.coderule.minimq.broker.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.consumer.ConsumerManager;
import cn.coderule.minimq.broker.domain.producer.ProducerManager;
import cn.coderule.minimq.broker.domain.meta.MetaManager;
import cn.coderule.minimq.broker.domain.timer.TimerManager;
import cn.coderule.minimq.broker.domain.transaction.TransactionManager;
import cn.coderule.minimq.broker.infra.BrokerRegister;
import cn.coderule.minimq.broker.infra.embed.EmbedStoreManager;
import cn.coderule.minimq.broker.infra.remote.RemoteStoreManager;
import cn.coderule.minimq.broker.infra.store.StoreManager;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.broker.server.grpc.GrpcManager;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.minimq.rpc.registry.route.RouteLoader;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

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

        registerEmbedStore();
        registerRemoteStore();
        registerStore();
    }

    private void registerDomain() {
        registerRoute();

        registerProducer();
        registerConsumer();

        registerTimer();
        registerTransaction();
    }

    private void registerServer() {
        registerGrpc();
        registerRpc();
    }

    private void registerGrpc() {
        GrpcManager component = new GrpcManager();
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

    private void registerRouteLoader() {



    }

    private void registerEmbedStore() {
        EmbedStoreManager component = new EmbedStoreManager();
        manager.register(component);
    }

    private void registerRemoteStore() {
        RemoteStoreManager component = new RemoteStoreManager();
        manager.register(component);
    }

    private void registerStore() {
        StoreManager component = new StoreManager();
        manager.register(component);
    }

    private void registerRoute() {
        MetaManager component = new MetaManager();
        manager.register(component);
    }

    private void registerProducer() {
        ProducerManager component = new ProducerManager();
        manager.register(component);
    }

    private void registerConsumer() {
        ConsumerManager component = new ConsumerManager();
        manager.register(component);
    }

    private void registerTransaction() {
        TransactionManager component = new TransactionManager();
        manager.register(component);
    }

    private void registerTimer() {
        TimerManager component = new TimerManager();
        manager.register(component);
    }

}
