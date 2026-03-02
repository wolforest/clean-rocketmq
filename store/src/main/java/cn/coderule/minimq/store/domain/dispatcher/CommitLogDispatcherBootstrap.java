package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEventDispatcher;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class CommitLogDispatcherBootstrap implements Lifecycle {
    private DefaultCommitEventDispatcher dispatcher;

    @Override
    public void initialize() throws Exception {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        CheckPoint checkPoint = StoreContext.getCheckPoint();
        dispatcher = new DefaultCommitEventDispatcher(commitLog, checkPoint);

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
