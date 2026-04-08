package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;

public class IndexBootstrap implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        CommitHandlerManager handlerManager = StoreContext.getBean(CommitHandlerManager.class);
        IndexCommitHandler handler = new IndexCommitHandler();
        handlerManager.registerHandler(handler);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

}
