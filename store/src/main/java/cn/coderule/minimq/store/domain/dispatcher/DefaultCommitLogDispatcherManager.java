package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.service.store.manager.CommitLogDispatcherManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class DefaultCommitLogDispatcherManager implements CommitLogDispatcherManager {
    private DefaultCommitEventDispatcher dispatcher;

    @Override
    public void initialize() {
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        dispatcher = new DefaultCommitEventDispatcher(commitLog);

        StoreContext.register(dispatcher, CommitEventDispatcher.class);
    }

    @Override
    public void start() {
        dispatcher.start();
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
