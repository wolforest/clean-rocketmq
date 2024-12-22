package com.wolf.minimq.store.server;

import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.domain.service.store.manager.CommitLogManager;
import com.wolf.minimq.domain.service.store.manager.DispatcherManager;
import com.wolf.minimq.domain.service.store.manager.IndexManager;
import com.wolf.minimq.domain.service.store.manager.MessageManager;
import com.wolf.minimq.domain.service.store.manager.TimerManager;
import com.wolf.minimq.store.domain.commitlog.DefaultCommitLogManager;
import com.wolf.minimq.store.domain.dispatcher.DefaultDispatcherManager;
import com.wolf.minimq.store.domain.index.DefaultIndexManager;
import com.wolf.minimq.store.domain.message.DefaultMessageManager;
import com.wolf.minimq.store.domain.timer.DefaultTimerManager;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        StoreContext.register(register);

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
        CommitLogManager component = new DefaultCommitLogManager();
        manager.register(component);
        StoreContext.register(component, CommitLogManager.class);
    }

    private void registerDispatcher() {
        DispatcherManager component = new DefaultDispatcherManager();
        manager.register(component);
        StoreContext.register(component, DispatcherManager.class);
    }

    private void registerConsumeQueue() {
        MessageManager component = new DefaultMessageManager();
        manager.register(component);
        StoreContext.register(component, MessageManager.class);
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


}
