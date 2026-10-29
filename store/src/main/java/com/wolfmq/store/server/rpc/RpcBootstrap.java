package com.wolfmq.store.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.server.store.api.TimerStore;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import com.wolfmq.domain.config.network.RpcServerConfig;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.store.server.rpc.processor.ConsumeProcessor;
import com.wolfmq.store.server.rpc.processor.SubscriptionProcessor;
import com.wolfmq.store.server.rpc.processor.TimerProcessor;
import com.wolfmq.store.server.rpc.processor.TopicProcessor;
import com.wolfmq.store.server.rpc.server.ConfigConverter;
import com.wolfmq.store.server.rpc.server.ConnectionManager;
import com.wolfmq.store.server.rpc.server.ExecutorManager;
import com.wolfmq.store.server.rpc.server.StoreServer;
import java.util.concurrent.ExecutorService;

public class RpcBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private StoreConfig storeConfig;
    private ExecutorManager executorManager;
    private StoreServer storeServer;

    public RpcBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);

        initExecutor();
        initServer();
        initProcessor();
    }

    @Override
    public void start() throws Exception {
        if (null != executorManager) executorManager.start();
        if (null != storeServer) storeServer.start();
    }

    @Override
    public void shutdown() throws Exception {
        if (null != executorManager) executorManager.shutdown();
        if (null != storeServer) storeServer.shutdown();
    }


    private void initExecutor() {
        executorManager = new ExecutorManager(storeConfig);
        try {
            executorManager.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initServer() {
        ConnectionManager connectionManager = new ConnectionManager();
        RpcServerConfig serverConfig = ConfigConverter.toServerConfig(storeConfig);
        storeServer = new StoreServer(storeConfig, serverConfig, connectionManager);
    }

    private void initProcessor() {
        initTopicProcessor();
        initSubscriptionProcessor();
        initConsumeProcessor();

        initTimerProcessor();
    }

    private void initTopicProcessor() {
        TopicConfig topicConfig = storeConfig.getTopicConfig();
        TopicStore topicStore = context.getBean(TopicStore.class, ContextNamespace.APP);
        ExecutorService executor = executorManager.getAdminExecutor();

        TopicProcessor topicProcessor = new TopicProcessor(topicConfig, topicStore, executor);
        storeServer.registerProcessor(topicProcessor);
    }

    private void initConsumeProcessor() {
        ExecutorService executor = executorManager.getAdminExecutor();
        ConsumeOffsetStore offsetStore = context.getBean(ConsumeOffsetStore.class, ContextNamespace.APP);

        ConsumeProcessor consumeProcessor = new ConsumeProcessor(offsetStore, executor);
        storeServer.registerProcessor(consumeProcessor);
    }

    private void initTimerProcessor() {
        ExecutorService executor = executorManager.getAdminExecutor();
        TimerStore timerStore = context.getBean(TimerStore.class, ContextNamespace.APP);

        TimerProcessor timerProcessor = new TimerProcessor(timerStore, executor);
        storeServer.registerProcessor(timerProcessor);
    }

    private void initSubscriptionProcessor() {
        SubscriptionStore subscriptionStore = context.getBean(SubscriptionStore.class, ContextNamespace.APP);
        ExecutorService executor = executorManager.getAdminExecutor();

        SubscriptionProcessor subscriptionProcessor = new SubscriptionProcessor(subscriptionStore, executor);
        storeServer.registerProcessor(subscriptionProcessor);
    }

}
