package com.wolf.minimq.store.server;

import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.config.StoreConfig;
import com.wolf.minimq.domain.config.TimerConfig;

public class ContextInitializer {
    private final StoreArgument argument;

    public static void init(StoreArgument argument) {
        ContextInitializer initializer = new ContextInitializer(argument);
        initializer.initialize();
    }

    public ContextInitializer(StoreArgument argument) {
        this.argument = argument;
    }

    public void initialize() {
        initializeConfig();
    }

    private void initializeConfig() {
        StoreContext.register(new StoreConfig());
        StoreContext.register(new CommitLogConfig());
        StoreContext.register(new TimerConfig());
    }

    private void initializeMonitor() {
        if (null == argument.getMonitorContext()) {
            return;
        }

        StoreContext.MONITOR = argument.getMonitorContext();
    }

}
