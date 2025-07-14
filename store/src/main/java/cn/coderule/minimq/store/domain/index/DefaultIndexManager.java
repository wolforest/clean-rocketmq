package cn.coderule.minimq.store.domain.index;

import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.service.store.domain.index.IndexManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class DefaultIndexManager implements IndexManager {
    @Override
    public void initialize() throws Exception {
        CommitEventDispatcher dispatcher = StoreContext.getBean(CommitEventDispatcher.class);
        IndexCommitEventHandler handler = new IndexCommitEventHandler();
        dispatcher.registerHandler(handler);


    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }



    @Override
    public void cleanup() throws Exception {

    }

    @Override
    public State getState() {
        return null;
    }
}
