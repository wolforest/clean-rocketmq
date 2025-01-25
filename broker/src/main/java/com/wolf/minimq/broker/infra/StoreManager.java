package com.wolf.minimq.broker.infra;

import com.wolf.common.convention.container.ApplicationContext;
import com.wolf.common.convention.service.Lifecycle;
import com.wolf.minimq.broker.server.model.BrokerContext;
import com.wolf.minimq.store.Store;
import com.wolf.minimq.store.server.StoreArgument;

public class StoreManager implements Lifecycle {
    private Store store;

    @Override
    public void initialize() {
        StoreArgument storeArgument = new StoreArgument();
        store = new Store(storeArgument);
        store.initialize();
    }

    @Override
    public void start() {
        store.start();

        ApplicationContext storeApi = store.getAPIContext();
        BrokerContext.registerContext(storeApi);
    }

    @Override
    public void shutdown() {
        store.shutdown();
    }

    @Override
    public void cleanup() {
        store.cleanup();
    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
