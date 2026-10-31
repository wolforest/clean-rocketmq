package com.wolfmq.store.domain.dispatcher;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.store.domain.commitlog.log.CommitLogProxy;
import cn.coderule.common.convention.container.ApplicationContext;

/**
 * @renamed from CommitLogDispatcherBootstrap to DispatcherBootstrap
 */
public class DispatcherBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private DispatchManager dispatchManager;

    public DispatcherBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        CommitConfig commitConfig = storeConfig.getCommit();
        CheckPoint checkPoint = context.getBean(CheckPoint.class);
        CommitLogProxy commitLogProxy = context.getBean(CommitLogProxy.class);

        CommitHandlerManager handlerManager = new CommitHandlerManager();
        context.register(handlerManager);

        dispatchManager = new DispatchManager(
            commitConfig, checkPoint, commitLogProxy, handlerManager);
        context.register(dispatchManager);
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
