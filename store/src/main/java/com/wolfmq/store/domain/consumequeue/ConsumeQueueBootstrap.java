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
import com.wolfmq.store.server.bootstrap.StoreContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueBootstrap implements Lifecycle {
    private ConsumeQueueConfig consumeQueueConfig;
    private ConsumeQueueFlusher flusher;
    private ConsumeQueueLoader loader;
    private ConsumeQueueRecovery recovery;
    private ConsumeQueueManager consumeQueueManager;

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
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        consumeQueueConfig = storeConfig.getConsumeQueueConfig();
    }

    private void initConsumeQueue() {
        flusher = new ConsumeQueueFlusher(consumeQueueConfig, StoreContext.getCheckPoint());
        loader = new ConsumeQueueLoader(consumeQueueConfig);

        CommitConfig commitConfig = StoreContext.getBean(StoreConfig.class).getCommitConfig();
        OffsetCodec offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
        recovery = new ConsumeQueueRecovery(consumeQueueConfig, StoreContext.getCheckPoint(), offsetCodec);

        ConsumeQueueFactory consumeQueueFactory = initConsumeQueueFactory();
        consumeQueueManager = new ConsumeQueueManager(consumeQueueFactory);
        StoreContext.register(consumeQueueManager, ConsumeQueueManager.class);
    }

    private ConsumeQueueFactory initConsumeQueueFactory() {
        TopicService topicService = StoreContext.getBean(TopicService.class);
        ConsumeQueueFactory consumeQueueFactory = new ConsumeQueueFactory(
            consumeQueueConfig,
            topicService,
            StoreContext.getCheckPoint()
        );

        consumeQueueFactory.addCreateHook(flusher);
        consumeQueueFactory.addCreateHook(loader);
        consumeQueueFactory.addCreateHook(recovery);

        consumeQueueFactory.createAll();
        return consumeQueueFactory;
    }

    private void registerDispatchHandler() {
        CommitHandlerManager handlerManager = StoreContext.getBean(CommitHandlerManager.class);
        ConsumeQueueCommitHandler handler = new ConsumeQueueCommitHandler(consumeQueueManager);
        handlerManager.registerHandler(handler);
    }
}
