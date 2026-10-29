package com.wolfmq.store.domain.dispatcher;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;

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
        StoreConfig storeConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        CheckPoint checkPoint = context.getBean(CheckPoint.class, ContextNamespace.APP);
        CommitLogManager commitLogManager = context.getBean(CommitLogManager.class, ContextNamespace.APP);

        CommitHandlerManager handlerManager = new CommitHandlerManager();
        context.register(handlerManager, ContextNamespace.APP);

        dispatchManager = new DispatchManager(
            commitConfig, checkPoint, commitLogManager, handlerManager);
        context.register(dispatchManager, ContextNamespace.APP);
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
