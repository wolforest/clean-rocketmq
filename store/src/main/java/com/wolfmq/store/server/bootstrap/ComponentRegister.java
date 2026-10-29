package com.wolfmq.store.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.container.EventBus;
import cn.coderule.common.convention.service.LifecycleManager;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.rpc.common.rpc.netty.NettyClient;
import com.wolfmq.store.domain.commitlog.CommitLogBootstrap;
import com.wolfmq.store.domain.consumequeue.ConsumeQueueBootstrap;
import com.wolfmq.store.domain.dispatcher.DispatcherBootstrap;
import com.wolfmq.store.domain.index.IndexBootstrap;
import com.wolfmq.store.domain.mq.MQBootstrap;
import com.wolfmq.store.domain.meta.MetaBootstrap;
import com.wolfmq.store.domain.timer.TimerBootstrap;
import com.wolfmq.store.infra.StoreScheduler;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import com.wolfmq.store.infra.memory.TransientPool;
import com.wolfmq.store.server.ha.HABootstrap;
import com.wolfmq.store.server.rpc.RpcBootstrap;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();
    private final ApplicationContext context;

    public ComponentRegister(ApplicationContext context) {
        this.context = context;
    }

    public LifecycleManager register() {
        context.register(this, ContextNamespace.APP);

        return this.execute();
    }

    public LifecycleManager execute() {
        registerLib();
        registerInfra();
        registerDomain();
        registerServer();

        return this.manager;
    }

    private void registerLib() {
        EventBus eventBus = new EventBus();
        context.register(eventBus, ContextNamespace.APP);
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
        StoreConfig storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        NettyClient component = new NettyClient(storeConfig.getRpcClientConfig());
        context.register(component, ContextNamespace.APP);
    }

    private void registerScheduler() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        StoreScheduler scheduler = new StoreScheduler(storeConfig);

        manager.register(scheduler);
        context.register(scheduler, StoreScheduler.class, ContextNamespace.APP);
    }

    private void registerMeta() {
        MetaBootstrap component = new MetaBootstrap(context);
        manager.register(component);
    }

    private void registerCommitLog() {
        CommitLogBootstrap component = new CommitLogBootstrap(context);
        manager.register(component);
    }

    private void registerDispatcher() {
        DispatcherBootstrap component = new DispatcherBootstrap(context);
        manager.register(component);
    }

    private void registerConsumeQueue() {
        ConsumeQueueBootstrap component = new ConsumeQueueBootstrap(context);
        manager.register(component);
    }

    private void registerMessageQueue() {
        MQBootstrap component = new MQBootstrap(context);
        manager.register(component);
    }

    private void registerIndexService() {
        IndexBootstrap component = new IndexBootstrap(context);
        manager.register(component);
    }

    private void registerTimer() {
        TimerBootstrap component = new TimerBootstrap(context);
        manager.register(component);
    }

    private void registerMappedFileService() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        TransientPool transientPool = null;
        if (storeConfig.isEnableTransientPool()) {
            transientPool = new TransientPool(storeConfig.getTransientPoolSize(), storeConfig.getTransientFileSize());
            manager.register(transientPool);
            context.register(transientPool, ContextNamespace.APP);
        }

        AllocateMappedFileService component = new AllocateMappedFileService(storeConfig, transientPool);
        manager.register(component);
        context.register(component, ContextNamespace.APP);
    }

    private void registerRegistry() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        NettyClient nettyClient = context.getBean(NettyClient.class, ContextNamespace.APP);
        StoreRegister component = new StoreRegister(
            storeConfig, nettyClient,
            () -> context.getBean(TopicService.class, ContextNamespace.APP)
        );

        manager.register(component);
        context.register(component, ContextNamespace.APP);
    }

    private void registerRpc() {
        RpcBootstrap component = new RpcBootstrap(context);
        manager.register(component);
    }

    private void registerHA() {
        HABootstrap component = new HABootstrap(context);
        manager.register(component);
    }

}
