package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.minimq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import cn.coderule.minimq.store.domain.consumequeue.service.ConsumeQueueLoader;
import cn.coderule.minimq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import cn.coderule.minimq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
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

    @Override
    public void cleanup() throws Exception {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initConfig() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        consumeQueueConfig = storeConfig.getConsumeQueueConfig();
    }

    private void initConsumeQueue() {
        flusher = new ConsumeQueueFlusher(consumeQueueConfig, StoreContext.getCheckPoint());
        loader = new ConsumeQueueLoader(consumeQueueConfig);
        recovery = new ConsumeQueueRecovery(consumeQueueConfig, StoreContext.getCheckPoint());

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
