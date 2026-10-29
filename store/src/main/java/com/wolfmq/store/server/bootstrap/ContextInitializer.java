package com.wolfmq.store.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.domain.server.store.StoreState;
import com.wolfmq.domain.config.network.RpcClientConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.config.store.IndexConfig;
import com.wolfmq.domain.config.store.MetaConfig;
import com.wolfmq.domain.config.store.StorePath;

public class ContextInitializer {
    private final StoreArgument argument;
    private final ApplicationContext context;

    public static void init(StoreArgument argument, ApplicationContext context) {
        ContextInitializer initializer = new ContextInitializer(argument, context);
        initializer.initialize();
    }

    public ContextInitializer(StoreArgument argument, ApplicationContext context) {
        this.argument = argument;
        this.context = context;
    }

    public void initialize() {
        this.argument.validate();

        initializeConfig();
        initializeMonitor();
    }

    private void initializeConfig() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessageConfig(new MessageConfig());
        storeConfig.setTopicConfig(new TopicConfig());
        storeConfig.setCommitConfig(new CommitConfig());
        storeConfig.setConsumeQueueConfig(new ConsumeQueueConfig());
        storeConfig.setTimerConfig(new TimerConfig());
        storeConfig.setMetaConfig(new MetaConfig());
        storeConfig.setRpcClientConfig(new RpcClientConfig());
        storeConfig.setIndexConfig(new IndexConfig());

        StorePath.initPath(storeConfig.getRootDir());

        context.register(storeConfig, ContextNamespace.APP);
        context.register(new StoreState(), StoreState.class, ContextNamespace.APP);
    }

    private void initializeMonitor() {
        if (null == argument.getMonitorContext()) {
            return;
        }

        context.merge(argument.getMonitorContext(), ContextNamespace.MONITOR);
    }

}
