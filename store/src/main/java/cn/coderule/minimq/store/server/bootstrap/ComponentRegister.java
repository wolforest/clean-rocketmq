package cn.coderule.minimq.store.server.bootstrap;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.service.store.manager.CommitLogManager;
import cn.coderule.minimq.domain.service.store.manager.CommitLogDispatcherManager;
import cn.coderule.minimq.domain.service.store.manager.ConsumeQueueManager;
import cn.coderule.minimq.domain.service.store.manager.IndexManager;
import cn.coderule.minimq.domain.service.store.manager.MessageQueueManager;
import cn.coderule.minimq.domain.service.store.manager.MetaManager;
import cn.coderule.minimq.domain.service.store.manager.TimerManager;
import cn.coderule.minimq.store.domain.commitlog.DefaultCommitLogManager;
import cn.coderule.minimq.store.domain.consumequeue.DefaultConsumeQueueManager;
import cn.coderule.minimq.store.domain.dispatcher.DefaultCommitLogDispatcherManager;
import cn.coderule.minimq.store.domain.index.DefaultIndexManager;
import cn.coderule.minimq.store.domain.mq.DefaultMessageQueueManager;
import cn.coderule.minimq.store.domain.meta.DefaultMetaManager;
import cn.coderule.minimq.store.domain.timer.DefaultTimerManager;
import cn.coderule.minimq.store.infra.StoreRegister;
import cn.coderule.minimq.store.infra.file.AllocateMappedFileService;
import cn.coderule.minimq.store.infra.memory.TransientPool;
import cn.coderule.minimq.store.server.StoreContext;
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
        registerBaseComponent();

        registerInfra();
        registerDomain();
        registerServer();

        return this.manager;
    }

    private void registerInfra() {
        registerMappedFileService();
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
        registerRegistry();
    }

    private void registerBaseComponent() {
        registerScheduler();
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
        MessageQueueManager component = new DefaultMessageQueueManager();
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

    private void registerRpc() {
        RpcManager component = new RpcManager();
        manager.register(component);
    }

    private void registerHA() {
        HAManager component = new HAManager();
        manager.register(component);
    }

    private void registerRegistry() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        StoreRegister component = new StoreRegister(storeConfig);
        manager.register(component);
        StoreContext.register(component);
    }
}
