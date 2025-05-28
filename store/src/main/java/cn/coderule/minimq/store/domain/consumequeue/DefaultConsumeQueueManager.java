package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.config.ConsumeQueueConfig;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.manager.ConsumeQueueManager;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import cn.coderule.minimq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import cn.coderule.minimq.store.domain.consumequeue.service.ConsumeQueueLoader;
import cn.coderule.minimq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.bootstrap.StorePath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultConsumeQueueManager implements ConsumeQueueManager {
    private ConsumeQueueConfig consumeQueueConfig;
    private ConsumeQueueFlusher flusher;
    private ConsumeQueueLoader loader;
    private ConsumeQueueRecovery recovery;
    private ConsumeQueueGateway consumeQueueGateway;

    @Override
    public void initialize() {
        initConfig();
        initConsumeQueue();

        loader.load();
        recovery.recover();

        registerDispatchHandler();
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

    private void initConsumeQueue() {
        flusher = new ConsumeQueueFlusher(consumeQueueConfig, StoreContext.getCheckPoint());
        loader = new ConsumeQueueLoader(consumeQueueConfig);
        recovery = new ConsumeQueueRecovery(consumeQueueConfig, StoreContext.getCheckPoint());

        ConsumeQueueFactory consumeQueueFactory = initConsumeQueueFactory();
        consumeQueueGateway = new DefaultConsumeQueueGateway(consumeQueueFactory);
        StoreContext.register(consumeQueueGateway, ConsumeQueueGateway.class);
    }

    private ConsumeQueueFactory initConsumeQueueFactory() {
        TopicService topicService = StoreContext.getBean(TopicService.class);
        ConsumeQueueFactory consumeQueueFactory = new ConsumeQueueFactory(consumeQueueConfig, topicService, StoreContext.getCheckPoint());

        consumeQueueFactory.addCreateHook(flusher);
        consumeQueueFactory.addCreateHook(loader);
        consumeQueueFactory.addCreateHook(recovery);

        consumeQueueFactory.createAll();
        return consumeQueueFactory;
    }

    private void registerDispatchHandler() {
        CommitEventDispatcher dispatcher = StoreContext.getBean(CommitEventDispatcher.class);
        QueueCommitEventHandler handler = new QueueCommitEventHandler(consumeQueueGateway);
        dispatcher.registerHandler(handler);
    }
}
