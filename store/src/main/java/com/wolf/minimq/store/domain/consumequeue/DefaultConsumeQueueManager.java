package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.config.ConsumeQueueConfig;
import com.wolf.minimq.domain.service.store.domain.CommitLogDispatcher;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueueStore;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;
import com.wolf.minimq.domain.service.store.manager.ConsumeQueueManager;
import com.wolf.minimq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import com.wolf.minimq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import com.wolf.minimq.store.domain.consumequeue.service.ConsumeQueueLoader;
import com.wolf.minimq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import com.wolf.minimq.store.server.StoreContext;
import com.wolf.minimq.store.server.StorePath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultConsumeQueueManager implements ConsumeQueueManager {
    private ConsumeQueueConfig consumeQueueConfig;
    private ConsumeQueueFlusher flusher;
    private ConsumeQueueLoader loader;
    private ConsumeQueueRecovery recovery;
    private ConsumeQueueStore consumeQueueStore;

    @Override
    public void initialize() {
        initConfig();
        initObjects();

        loader.load();
        recovery.recover();

        registerDispatchHandler(consumeQueueStore);
    }

    @Override
    public void start() {
        flusher.start();
    }

    @Override
    public void shutdown() {
        flusher.shutdown();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initConfig() {
        consumeQueueConfig = StoreContext.getBean(ConsumeQueueConfig.class);
        consumeQueueConfig.setRootPath(StorePath.getConsumeQueuePath());
    }

    private void initObjects() {
        flusher = new ConsumeQueueFlusher(consumeQueueConfig, StoreContext.getCheckPoint());
        loader = new ConsumeQueueLoader(consumeQueueConfig);
        recovery = new ConsumeQueueRecovery(consumeQueueConfig, StoreContext.getCheckPoint());

        ConsumeQueueFactory consumeQueueFactory = initConsumeQueueFactory();
        consumeQueueStore = new DefaultConsumeQueueStore(consumeQueueFactory);
        StoreContext.register(consumeQueueStore, ConsumeQueueStore.class);
    }

    private ConsumeQueueFactory initConsumeQueueFactory() {
        TopicStore topicStore = StoreContext.getBean(TopicStore.class);
        ConsumeQueueFactory consumeQueueFactory = new ConsumeQueueFactory(consumeQueueConfig, topicStore, StoreContext.getCheckPoint());

        consumeQueueFactory.addCreateHook(flusher);
        consumeQueueFactory.addCreateHook(loader);
        consumeQueueFactory.addCreateHook(recovery);

        consumeQueueFactory.createAll();
        return consumeQueueFactory;
    }

    private void registerDispatchHandler(ConsumeQueueStore consumeQueueStore) {
        CommitLogDispatcher dispatcher = StoreContext.getBean(CommitLogDispatcher.class);
        QueueCommitLogHandler handler = new QueueCommitLogHandler(consumeQueueStore);
        dispatcher.registerHandler(handler);
    }
}
