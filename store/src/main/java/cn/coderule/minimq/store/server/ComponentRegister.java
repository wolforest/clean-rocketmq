package cn.coderule.minimq.store.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.service.common.ServerEventBus;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogManager;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogDispatcherManager;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueManager;
import cn.coderule.minimq.domain.service.store.domain.index.IndexManager;
import cn.coderule.minimq.domain.service.store.domain.mq.MQManager;
import cn.coderule.minimq.domain.service.store.domain.meta.MetaManager;
import cn.coderule.minimq.domain.service.store.domain.timer.TimerManager;
import cn.coderule.minimq.store.domain.commitlog.DefaultCommitLogManager;
import cn.coderule.minimq.store.domain.consumequeue.DefaultConsumeQueueManager;
import cn.coderule.minimq.store.domain.dispatcher.DefaultCommitLogDispatcherManager;
import cn.coderule.minimq.store.domain.index.DefaultIndexManager;
import cn.coderule.minimq.store.domain.mq.DefaultMQManager;
import cn.coderule.minimq.store.domain.meta.DefaultMetaManager;
import cn.coderule.minimq.store.domain.timer.DefaultTimerManager;
import cn.coderule.minimq.store.infra.StoreRegister;
import cn.coderule.minimq.store.infra.StoreScheduler;
import cn.coderule.minimq.store.infra.file.AllocateMappedFileService;
import cn.coderule.minimq.store.infra.memory.TransientPool;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.HAManager;
import cn.coderule.minimq.store.server.rpc.RpcManager;

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

    private void registerScheduler() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        StoreScheduler scheduler = new StoreScheduler(storeConfig);

        manager.register(scheduler);
        StoreContext.setScheduler(scheduler);
    }

    private void registerMeta() {
        MetaManager component = new DefaultMetaManager();
        manager.register(component);
    }

    private void registerCommitLog() {
        CommitLogManager component = new DefaultCommitLogManager();
        manager.register(component);
    }

    private void registerDispatcher() {
        CommitLogDispatcherManager component = new DefaultCommitLogDispatcherManager();
        manager.register(component);
    }

    private void registerConsumeQueue() {
        ConsumeQueueManager component = new DefaultConsumeQueueManager();
        manager.register(component);
    }

    private void registerMessageQueue() {
        MQManager component = new DefaultMQManager();
        manager.register(component);
    }

    private void registerIndexService() {
        IndexManager component = new DefaultIndexManager();
        manager.register(component);
    }

    private void registerTimer() {
        TimerManager component = new DefaultTimerManager();
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
        StoreRegister component = new StoreRegister(storeConfig);

        manager.register(component);
        StoreContext.register(component);
    }

    private void registerRpc() {
        RpcManager component = new RpcManager();
        manager.register(component);
    }

    private void registerHA() {
        HAManager component = new HAManager();
        manager.register(component);
    }

}
