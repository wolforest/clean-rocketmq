package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.service.store.domain.CommitLogDispatcher;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueueStore;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;
import com.wolf.minimq.domain.service.store.manager.ConsumeQueueManager;
import com.wolf.minimq.store.server.StoreContext;

public class DefaultConsumeQueueManager implements ConsumeQueueManager {
    private ConsumeQueueLoader loader;
    private ConsumeQueueFlusher flusher;

    @Override
    public void initialize() {
        TopicStore topicStore = StoreContext.getBean(TopicStore.class);
        flusher = new ConsumeQueueFlusher();
        loader = new ConsumeQueueLoader();

        ConsumeQueueFactory consumeQueueFactory = new ConsumeQueueFactory(topicStore, flusher, loader);
        consumeQueueFactory.createAll();

        ConsumeQueueStore consumeQueueStore = new DefaultConsumeQueueStore(consumeQueueFactory);
        StoreContext.register(consumeQueueStore, ConsumeQueueStore.class);

        CommitLogDispatcher dispatcher = StoreContext.getBean(CommitLogDispatcher.class);
        QueueCommitLogHandler handler = new QueueCommitLogHandler(consumeQueueStore);
        dispatcher.registerHandler(handler);

        loader.load();
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }



    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return null;
    }
}
