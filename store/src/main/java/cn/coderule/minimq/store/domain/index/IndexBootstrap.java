package cn.coderule.minimq.store.domain.index;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class IndexBootstrap implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        CommitEventDispatcher dispatcher = StoreContext.getBean(CommitEventDispatcher.class);
        IndexCommitHandler handler = new IndexCommitHandler();
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
