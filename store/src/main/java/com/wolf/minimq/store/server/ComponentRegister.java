package com.wolf.minimq.store.server;

import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.store.domain.commitlog.CommitLogManager;
import com.wolf.minimq.store.domain.dispatcher.DispatcherManager;
import com.wolf.minimq.store.domain.index.IndexManager;
import com.wolf.minimq.store.domain.queue.MessageQueueManager;
import com.wolf.minimq.store.domain.timer.TimerManager;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();

        return register.execute();
    }

    public LifecycleManager execute() {
        registerCommitLog();
        registerDispatcher();
        registerConsumeQueue();
        registerIndexService();
        registerTimer();

        return this.manager;
    }

    private void registerCommitLog() {
        CommitLogManager component = new CommitLogManager();
        manager.register(component);
        StoreContext.register(component, CommitLogManager.class);
    }

    private void registerDispatcher() {
        DispatcherManager component = new DispatcherManager();
        manager.register(component);
        StoreContext.register(component, DispatcherManager.class);
    }

    private void registerConsumeQueue() {
        MessageQueueManager component = new MessageQueueManager();
        manager.register(component);
        StoreContext.register(component, MessageQueueManager.class);
    }

    private void registerIndexService() {
        IndexManager component = new IndexManager();
        manager.register(component);
        StoreContext.register(component, IndexManager.class);
    }

    private void registerTimer() {
        TimerManager component = new TimerManager();
        manager.register(component);
        StoreContext.register(component, TimerManager.class);
    }


}
