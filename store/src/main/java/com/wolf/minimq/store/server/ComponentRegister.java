package com.wolf.minimq.store.server;

import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.domain.service.store.CommitLog;
import com.wolf.minimq.domain.service.store.ConsumeQueue;
import com.wolf.minimq.domain.service.store.Dispatcher;
import com.wolf.minimq.domain.service.store.IndexService;
import com.wolf.minimq.domain.service.store.Timer;
import com.wolf.minimq.store.domain.commitlog.DefaultCommitLog;
import com.wolf.minimq.store.domain.dispatcher.DefaultDispatcher;
import com.wolf.minimq.store.domain.index.DefaultIndexService;
import com.wolf.minimq.store.domain.queue.DefaultConsumeQueue;
import com.wolf.minimq.store.domain.timer.DefaultTimer;

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
        CommitLog component = new DefaultCommitLog();
        manager.register(component);
        StoreContext.register(component, CommitLog.class);
    }

    private void registerDispatcher() {
        Dispatcher component = new DefaultDispatcher();
        manager.register(component);
        StoreContext.register(component, Dispatcher.class);
    }

    private void registerConsumeQueue() {
        ConsumeQueue component = new DefaultConsumeQueue();
        manager.register(component);
        StoreContext.register(component, ConsumeQueue.class);
    }

    private void registerIndexService() {
        IndexService component = new DefaultIndexService();
        manager.register(component);
        StoreContext.register(component, IndexService.class);
    }

    private void registerTimer() {
        Timer component = new DefaultTimer();
        manager.register(component);
        StoreContext.register(component, Timer.class);
    }


}
