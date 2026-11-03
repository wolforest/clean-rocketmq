package com.wolfmq.store.domain.consumequeue;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFactory;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueFlusher;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueLoader;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueRecovery;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.server.store.checkpoint.bakeup.CheckPoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private ConsumeQueueConfig consumeQueueConfig;
    private CommitConfig commitConfig;

    private ConsumeQueueFlusher flusher;
    private ConsumeQueueLoader loader;
    private ConsumeQueueRecovery recovery;
    private ConsumeQueueFacade consumeQueueFacade;

    public ConsumeQueueBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        initConfig();
        initConsumeQueue();

        loader.load();
        recovery.recover();

        registerDispatchHandler();
    }

    @Override
    public void start() throws Exception {
        flusher.start();
    }

    @Override
    public void shutdown() throws Exception {
        flusher.shutdown();
    }

    private void initConfig() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        consumeQueueConfig = storeConfig.getConsumeQueue();
        commitConfig = storeConfig.getCommit();
    }

    private void initConsumeQueue() {
        flusher = new ConsumeQueueFlusher(consumeQueueConfig);
        loader = new ConsumeQueueLoader(consumeQueueConfig);
        CheckPoint checkPoint = context.getBean(CheckPoint.class);

        recovery = new ConsumeQueueRecovery(consumeQueueConfig, checkPoint);

        ConsumeQueueFactory consumeQueueFactory = initConsumeQueueFactory();
        consumeQueueFacade = new ConsumeQueueFacade(consumeQueueFactory);
        context.register(consumeQueueFacade);
    }

    private ConsumeQueueFactory initConsumeQueueFactory() {
        TopicService topicService = context.getBean(TopicService.class);
        ConsumeQueueFactory consumeQueueFactory = new ConsumeQueueFactory(
            consumeQueueConfig,
            topicService,
            context.getBean(CheckPoint.class)
        );

        consumeQueueFactory.addCreateHook(flusher);
        consumeQueueFactory.addCreateHook(loader);
        consumeQueueFactory.addCreateHook(recovery);

        consumeQueueFactory.createAll();
        return consumeQueueFactory;
    }

    private void registerDispatchHandler() {
        CommitHandlerManager handlerManager = context.getBean(CommitHandlerManager.class);
        ConsumeQueueCommitHandler handler = new ConsumeQueueCommitHandler(consumeQueueFacade);
        handlerManager.registerHandler(handler);
    }
}
