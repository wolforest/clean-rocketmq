package com.wolf.minimq.store.domain.queue;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.minimq.store.server.StoreContext;

public class MessageQueueManager implements Lifecycle {
    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize() {
        StoreContext.register(new MessageQueueDomainService());
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return null;
    }
}
