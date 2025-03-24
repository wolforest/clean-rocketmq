package cn.coderule.minimq.store.domain.index;

import cn.coderule.minimq.domain.service.store.domain.CommitLogDispatcher;
import cn.coderule.minimq.domain.service.store.manager.IndexManager;
import cn.coderule.minimq.store.server.StoreContext;

public class DefaultIndexManager implements IndexManager {
    @Override
    public void initialize() {
        CommitLogDispatcher dispatcher = StoreContext.getBean(CommitLogDispatcher.class);
        IndexCommitLogHandler handler = new IndexCommitLogHandler();
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
