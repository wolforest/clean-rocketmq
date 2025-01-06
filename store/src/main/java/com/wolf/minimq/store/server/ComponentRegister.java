package com.wolf.minimq.store.server;

import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.domain.config.StoreConfig;
import com.wolf.minimq.domain.service.store.manager.CommitLogManager;
import com.wolf.minimq.domain.service.store.manager.CommitLogDispatcherManager;
import com.wolf.minimq.domain.service.store.manager.IndexManager;
import com.wolf.minimq.domain.service.store.manager.MessageQueueManager;
import com.wolf.minimq.domain.service.store.manager.MetaManager;
import com.wolf.minimq.domain.service.store.manager.TimerManager;
import com.wolf.minimq.store.domain.commitlog.DefaultCommitLogManager;
import com.wolf.minimq.store.domain.dispatcher.DefaultCommitLogDispatcherManager;
import com.wolf.minimq.store.domain.index.DefaultIndexManager;
import com.wolf.minimq.store.domain.mq.DefaultMessageQueueManager;
import com.wolf.minimq.store.domain.meta.DefaultMetaManager;
import com.wolf.minimq.store.domain.timer.DefaultTimerManager;
import com.wolf.minimq.store.infra.file.AllocateMappedFileService;
import com.wolf.minimq.store.infra.memory.TransientPool;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        StoreContext.register(register);

        return register.execute();
    }

    public LifecycleManager execute() {
        registerMeta();
        registerCommitLog();
        registerDispatcher();
        registerConsumeQueue();
        registerIndexService();
        registerTimer();
        registerMappedFileService();

        return this.manager;
    }

    private void registerMeta() {
        MetaManager component = new DefaultMetaManager();
        manager.register(component);
        StoreContext.register(component, MetaManager.class);
    }

    private void registerCommitLog() {
        CommitLogManager component = new DefaultCommitLogManager();
        manager.register(component);
        StoreContext.register(component, CommitLogManager.class);
    }

    private void registerDispatcher() {
        CommitLogDispatcherManager component = new DefaultCommitLogDispatcherManager();
        manager.register(component);
        StoreContext.register(component, CommitLogDispatcherManager.class);
    }

    private void registerConsumeQueue() {
        MessageQueueManager component = new DefaultMessageQueueManager();
        manager.register(component);
        StoreContext.register(component, MessageQueueManager.class);
    }

    private void registerIndexService() {
        IndexManager component = new DefaultIndexManager();
        manager.register(component);
        StoreContext.register(component, IndexManager.class);
    }

    private void registerTimer() {
        TimerManager component = new DefaultTimerManager();
        manager.register(component);
        StoreContext.register(component, TimerManager.class);
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
}
