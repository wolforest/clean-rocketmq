package com.wolfmq.store.server.bootstrap;

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

    public static void init(StoreArgument argument) {
        ContextInitializer initializer = new ContextInitializer(argument);
        initializer.initialize();
    }

    public ContextInitializer(StoreArgument argument) {
        this.argument = argument;
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

        StoreContext.register(storeConfig);
    }

    private void initializeMonitor() {
        if (null == argument.getMonitorContext()) {
            return;
        }

        StoreContext.MONITOR = argument.getMonitorContext();
    }

}
