package cn.coderule.wolfmq.store.domain.consumequeue;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueLoader;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import cn.coderule.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
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

        CommitConfig commitConfig = StoreContext.getBean(CommitConfig.class);
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
