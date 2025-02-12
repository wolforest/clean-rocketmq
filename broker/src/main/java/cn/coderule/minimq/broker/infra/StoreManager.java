package cn.coderule.minimq.broker.infra;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.model.BrokerContext;
import cn.coderule.minimq.store.Store;
import cn.coderule.minimq.store.server.StoreArgument;

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
