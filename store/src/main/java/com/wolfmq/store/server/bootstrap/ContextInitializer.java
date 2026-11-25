package com.wolfmq.store.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.config.cli.StoreArgument;
import com.wolfmq.common.config.cli.StoreConfigLoader;
import com.wolfmq.common.core.constant.ContextNamespace;
import com.wolfmq.common.server.store.StoreState;
import com.wolfmq.common.config.network.RpcClientConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.config.business.MessageConfig;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.business.TimerConfig;
import com.wolfmq.common.config.business.TopicConfig;
import com.wolfmq.common.config.store.IndexConfig;
import com.wolfmq.common.config.store.MetaConfig;
import java.io.IOException;

public class ContextInitializer {
    private final StoreConfigLoader configLoader;
    private final ApplicationContext context;

    public static void init(StoreConfigLoader configLoader, ApplicationContext context) throws IOException {
        ContextInitializer initializer = new ContextInitializer(configLoader, context);
        initializer.initialize();
    }

    public ContextInitializer(StoreConfigLoader configLoader, ApplicationContext context) {
        this.configLoader = configLoader;
        this.context = context;
    }

    public void initialize() throws IOException {
        initializeConfig();
        initializeMonitor();

        StoreState storeState = new StoreState();
        context.register(storeState);
    }

    private void initializeConfig() throws IOException {
        StoreConfig storeConfig = configLoader.load();

        context.register(storeConfig);
    }

    private void initializeMonitor() {
        StoreArgument argument = configLoader.getArgument();
        if (null == argument.getMonitorContext()) {
            return;
        }

        context.merge(argument.getMonitorContext(), ContextNamespace.MONITOR);
    }

}
