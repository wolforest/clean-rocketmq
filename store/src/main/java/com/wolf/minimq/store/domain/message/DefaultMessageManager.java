package com.wolf.minimq.store.domain.message;

import com.wolf.minimq.domain.service.store.manager.MessageManager;
import com.wolf.minimq.domain.service.store.domain.MessageStore;
import com.wolf.minimq.store.server.StoreContext;

public class DefaultMessageManager implements MessageManager {
    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize() {
        StoreContext.register(new DefaultMessageStore(), MessageStore.class);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return null;
    }
}
