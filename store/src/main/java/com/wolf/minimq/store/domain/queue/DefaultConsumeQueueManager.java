package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.service.store.domain.CommitLogDispatcher;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;
import com.wolf.minimq.domain.service.store.manager.ConsumeQueueManager;
import com.wolf.minimq.store.domain.queue.store.QueueStoreManager;
import com.wolf.minimq.store.server.StoreContext;

public class DefaultConsumeQueueManager implements ConsumeQueueManager {
    @Override
    public void initialize() {
        TopicStore topicStore = StoreContext.getBean(TopicStore.class);
        QueueStoreManager queueStoreManager = new QueueStoreManager(topicStore);

        ConsumeQueue consumeQueue = new DefaultConsumeQueue(queueStoreManager);
        StoreContext.register(consumeQueue, ConsumeQueue.class);

        CommitLogDispatcher dispatcher = StoreContext.getBean(CommitLogDispatcher.class);
        QueueCommitLogHandler handler = new QueueCommitLogHandler(consumeQueue);
        dispatcher.registerHandler(handler);
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
