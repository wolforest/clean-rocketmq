package com.wolfmq.store.domain.commitlog;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.common.server.store.api.CommitLogStore;
import com.wolfmq.store.api.CommitLogStoreImpl;
import com.wolfmq.store.domain.commitlog.file.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.file.CommitLogManager;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.core.constant.ContextNamespace;

/**
 * depend on:
 *  - StoreConfig
 *  - CommitLogConfig
 */
public class CommitLogBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private CommitLogFacade commitLogFacade;
    private CommitLogManager logManager;

    public CommitLogBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        initLogManager();

        commitLogFacade = new CommitLogFacade(logManager);
        context.register(commitLogFacade);

        registerAPI();
        logManager.initialize();
    }

    @Override
    public void start() throws Exception {
        logManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        logManager.shutdown();
    }

    private void initLogManager() {
        CommitLogFactory commitLogFactory = createLogFactory();

        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        CommitConfig commitConfig = storeConfig.getCommit();

        logManager = new CommitLogManager(commitConfig, commitLogFactory);
        context.register(logManager);
    }

    private CommitLogFactory createLogFactory() {
        QueueProgress queueProgress = context.getBean(QueueProgress.class);
        StoreConfig storeConfig = context.getBean(StoreConfig.class);

        AllocateMappedFileService allocateService = context.getBean(AllocateMappedFileService.class);
        return new CommitLogFactory(storeConfig, queueProgress, allocateService);
    }

    private void registerAPI() {
        CommitLogStore api = new CommitLogStoreImpl(commitLogFacade);
        context.register(api, CommitLogStore.class, ContextNamespace.API);
    }
}
