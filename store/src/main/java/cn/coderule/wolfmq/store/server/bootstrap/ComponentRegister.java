package cn.coderule.wolfmq.store.server.bootstrap;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.core.event.ServerEventBus;
import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.wolfmq.store.domain.commitlog.CommitLogBootstrap;
import cn.coderule.wolfmq.store.domain.consumequeue.ConsumeQueueBootstrap;
import cn.coderule.wolfmq.store.domain.dispatcher.DispatcherBootstrap;
import cn.coderule.wolfmq.store.domain.index.IndexBootstrap;
import cn.coderule.wolfmq.store.domain.mq.MQBootstrap;
import cn.coderule.wolfmq.store.domain.meta.MetaBootstrap;
import cn.coderule.wolfmq.store.domain.timer.TimerBootstrap;
import cn.coderule.wolfmq.store.infra.StoreScheduler;
import cn.coderule.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.wolfmq.store.infra.memory.TransientPool;
import cn.coderule.wolfmq.store.server.ha.HABootstrap;
import cn.coderule.wolfmq.store.server.rpc.RpcBootstrap;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        StoreContext.register(register);

        return register.execute();
    }

    public LifecycleManager execute() {
        registerLib();
        registerInfra();
        registerDomain();
        registerServer();

        return this.manager;
    }

    private void registerLib() {
        ServerEventBus manager = new ServerEventBus();
        StoreContext.register(manager);
    }

    private void registerInfra() {
        registerNettyClient();
        registerScheduler();
        registerMappedFileService();
        registerRegistry();
    }

    private void registerDomain() {
        registerMeta();
        registerCommitLog();
        registerDispatcher();

        registerConsumeQueue();
        registerIndexService();

        registerMessageQueue();
        registerTimer();
    }

    private void registerServer() {
        registerRpc();
        registerHA();
    }

    private void registerNettyClient() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        NettyClient component = new NettyClient(storeConfig.getRpcClientConfig());
        StoreContext.register(component);
    }

    private void registerScheduler() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        StoreScheduler scheduler = new StoreScheduler(storeConfig);

        manager.register(scheduler);
        StoreContext.setScheduler(scheduler);
    }

    private void registerMeta() {
        MetaBootstrap component = new MetaBootstrap();
        manager.register(component);
    }

    private void registerCommitLog() {
        CommitLogBootstrap component = new CommitLogBootstrap();
        manager.register(component);
    }

    private void registerDispatcher() {
        DispatcherBootstrap component = new DispatcherBootstrap();
        manager.register(component);
    }

    private void registerConsumeQueue() {
        ConsumeQueueBootstrap component = new ConsumeQueueBootstrap();
        manager.register(component);
    }

    private void registerMessageQueue() {
        MQBootstrap component = new MQBootstrap();
        manager.register(component);
    }

    private void registerIndexService() {
        IndexBootstrap component = new IndexBootstrap();
        manager.register(component);
    }

    private void registerTimer() {
        TimerBootstrap component = new TimerBootstrap();
        manager.register(component);
    }

    private void registerMappedFileService() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        TransientPool transientPool = null;
        if (storeConfig.isEnableTransientPool()) {
            transientPool = new TransientPool(storeConfig.getTransientPoolSize(), storeConfig.getTransientFileSize());
            manager.register(transientPool);
            StoreContext.register(transientPool);
        }

        AllocateMappedFileService component = new AllocateMappedFileService(storeConfig, transientPool);
        manager.register(component);
        StoreContext.register(component);
    }

    private void registerRegistry() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        NettyClient nettyClient = StoreContext.getBean(NettyClient.class);
        StoreRegister component = new StoreRegister(storeConfig, nettyClient);

        manager.register(component);
        StoreContext.register(component);
    }

    private void registerRpc() {
        RpcBootstrap component = new RpcBootstrap();
        manager.register(component);
    }

    private void registerHA() {
        HABootstrap component = new HABootstrap();
        manager.register(component);
    }

}
