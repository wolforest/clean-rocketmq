package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import cn.coderule.minimq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

/**
 * @renamed from CommitLogDispatcherBootstrap to DispatcherBootstrap
 */
public class DispatcherBootstrap implements Lifecycle {
    private DispatchManager dispatchManager;

    @Override
    public void initialize() throws Exception {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        CheckPoint checkPoint = StoreContext.getCheckPoint();
        CommitLogManager commitLogManager = StoreContext.getBean(CommitLogManager.class);

        CommitHandlerManager handlerManager = new CommitHandlerManager();
        StoreContext.register(handlerManager);

        dispatchManager = new DispatchManager(
            commitConfig, checkPoint, commitLogManager, handlerManager);
        StoreContext.register(dispatchManager);
    }

    @Override
    public void start() throws Exception {
        dispatchManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        dispatchManager.shutdown();
    }
}
