package com.wolfmq.store.domain.consumequeue;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueLoader;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private ConsumeQueueConfig consumeQueueConfig;
    private ConsumeQueueFlusher flusher;
    private ConsumeQueueLoader loader;
    private ConsumeQueueRecovery recovery;
    private ConsumeQueueManager consumeQueueManager;

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
        StoreConfig storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        consumeQueueConfig = storeConfig.getConsumeQueueConfig();
    }

    private void initConsumeQueue() {
        flusher = new ConsumeQueueFlusher(consumeQueueConfig, context.getBean(StoreCheckpoint.class, ContextNamespace.APP));
        loader = new ConsumeQueueLoader(consumeQueueConfig);

        CommitConfig commitConfig = context.getBean(StoreConfig.class, ContextNamespace.APP).getCommitConfig();
        OffsetCodec offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
        recovery = new ConsumeQueueRecovery(consumeQueueConfig, context.getBean(StoreCheckpoint.class, ContextNamespace.APP), offsetCodec);

        ConsumeQueueFactory consumeQueueFactory = initConsumeQueueFactory();
        consumeQueueManager = new ConsumeQueueManager(consumeQueueFactory);
        context.register(consumeQueueManager, ConsumeQueueManager.class, ContextNamespace.APP);
    }

    private ConsumeQueueFactory initConsumeQueueFactory() {
        TopicService topicService = context.getBean(TopicService.class, ContextNamespace.APP);
        ConsumeQueueFactory consumeQueueFactory = new ConsumeQueueFactory(
            consumeQueueConfig,
            topicService,
            context.getBean(StoreCheckpoint.class, ContextNamespace.APP)
        );

        consumeQueueFactory.addCreateHook(flusher);
        consumeQueueFactory.addCreateHook(loader);
        consumeQueueFactory.addCreateHook(recovery);

        consumeQueueFactory.createAll();
        return consumeQueueFactory;
    }

    private void registerDispatchHandler() {
        CommitHandlerManager handlerManager = context.getBean(CommitHandlerManager.class, ContextNamespace.APP);
        ConsumeQueueCommitHandler handler = new ConsumeQueueCommitHandler(consumeQueueManager);
        handlerManager.registerHandler(handler);
    }
}
