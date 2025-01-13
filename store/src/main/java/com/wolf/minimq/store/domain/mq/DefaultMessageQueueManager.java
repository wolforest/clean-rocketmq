package com.wolf.minimq.store.domain.mq;

import com.wolf.minimq.domain.service.store.manager.MessageQueueManager;
import com.wolf.minimq.domain.service.store.domain.MessageQueue;
import com.wolf.minimq.store.server.StoreContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageQueueManager implements MessageQueueManager {
    @Override
    public void initialize() {
        StoreContext.register(new DefaultMessageQueue(), MessageQueue.class);
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
        return State.RUNNING;
    }
}
