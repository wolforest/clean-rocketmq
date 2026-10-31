package com.wolfmq.store.domain.consumequeue;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.domain.commitlog.route.OffsetCodec;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueProxy;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueLoader;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.server.store.CheckPoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private ConsumeQueueConfig consumeQueueConfig;
    private ConsumeQueueFlusher flusher;
    private ConsumeQueueLoader loader;
    private ConsumeQueueRecovery recovery;
    private ConsumeQueueProxy consumeQueueProxy;

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
    }

    private void initConsumeQueue() {
        flusher = new ConsumeQueueFlusher(consumeQueueConfig);
        loader = new ConsumeQueueLoader(consumeQueueConfig);

        CommitConfig commitConfig = context.getBean(StoreConfig.class).getCommit();
        OffsetCodec offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
        recovery = new ConsumeQueueRecovery(consumeQueueConfig, context.getBean(CheckPoint.class), offsetCodec);

        ConsumeQueueFactory consumeQueueFactory = initConsumeQueueFactory();
        consumeQueueProxy = new ConsumeQueueProxy(consumeQueueFactory);
        context.register(consumeQueueProxy, ConsumeQueueProxy.class);
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
        ConsumeQueueCommitHandler handler = new ConsumeQueueCommitHandler(consumeQueueProxy);
        handlerManager.registerHandler(handler);
    }
}
