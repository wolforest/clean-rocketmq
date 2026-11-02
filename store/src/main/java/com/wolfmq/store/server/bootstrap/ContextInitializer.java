package com.wolfmq.store.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
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
import com.wolfmq.common.config.store.StorePath;

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
        storeConfig.setMessage(new MessageConfig());
        storeConfig.setTopic(new TopicConfig());
        storeConfig.setCommit(new CommitConfig());
        storeConfig.setConsumeQueue(new ConsumeQueueConfig());
        storeConfig.setTimer(new TimerConfig());
        storeConfig.setMeta(new MetaConfig());
        storeConfig.setRpcClient(new RpcClientConfig());
        storeConfig.setIndex(new IndexConfig());

        StorePath.initPath(storeConfig.getRootDir());

        context.register(storeConfig);
        context.register(new StoreState(), StoreState.class);
    }

    private void initializeMonitor() {
        if (null == argument.getMonitorContext()) {
            return;
        }

        context.merge(argument.getMonitorContext(), ContextNamespace.MONITOR);
    }

}
