package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogDispatcherManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class DefaultCommitLogDispatcherManager implements CommitLogDispatcherManager {
    private DefaultCommitEventDispatcher dispatcher;

    @Override
    public void initialize() throws Exception {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        dispatcher = new DefaultCommitEventDispatcher(storeConfig, commitLog);

        StoreContext.register(dispatcher, CommitEventDispatcher.class);
    }

    @Override
    public void start() throws Exception {
        dispatcher.start();
    }

    @Override
    public void shutdown() throws Exception {
        dispatcher.stop();
    }

}
