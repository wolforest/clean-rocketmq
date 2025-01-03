package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.service.store.domain.CommitLogDispatcher;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.manager.ConsumeQueueManager;
import com.wolf.minimq.store.server.StoreContext;

public class DefaultConsumeQueueManager implements ConsumeQueueManager {
    @Override
    public void initialize() {
        CommitLogDispatcher dispatcher = StoreContext.getBean(CommitLogDispatcher.class);
        QueueCommitLogHandler handler = new QueueCommitLogHandler();
        dispatcher.registerHandler(handler);

        QueueStoreManager queueStoreManager = new QueueStoreManager();
        StoreContext.register(new DefaultConsumeQueue(queueStoreManager), ConsumeQueue.class);
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
