package cn.coderule.minimq.store.domain.index;

import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.service.store.domain.index.IndexManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class DefaultIndexManager implements IndexManager {
    @Override
    public void initialize() {
        CommitEventDispatcher dispatcher = StoreContext.getBean(CommitEventDispatcher.class);
        IndexCommitEventHandler handler = new IndexCommitEventHandler();
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
