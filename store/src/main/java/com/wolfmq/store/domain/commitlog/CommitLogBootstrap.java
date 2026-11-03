package com.wolfmq.store.domain.commitlog;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.server.store.api.CommitLogStore;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.common.server.store.checkpoint.bakeup.CheckPoint;
import com.wolfmq.store.api.CommitLogStoreImpl;
import com.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.route.TopicPartitioner;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.core.constant.ContextNamespace;
import java.util.List;

/**
 * depend on:
 *  - StoreConfig
 *  - CommitLogConfig
 */
public class CommitLogBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private CommitLogFacade commitLogFacade;

    public CommitLogBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        initCommitLog();
        commitLogFacade.initialize();

        registerAPI();
    }

    @Override
    public void start() throws Exception {
        commitLogFacade.start();
    }

    @Override
    public void shutdown() throws Exception {
        commitLogFacade.shutdown();
    }

    private void initCommitLog() {
        CheckPoint checkpoint = context.getBean(CheckPoint.class);
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        CommitConfig commitConfig = storeConfig.getCommit();
        TopicService topicService = context.getBean(TopicService.class);

        AllocateMappedFileService allocateService = context.getBean(AllocateMappedFileService.class);
        CommitLogFactory commitLogFactory = new CommitLogFactory(storeConfig, checkpoint, allocateService);
        List<CommitLog> logList = commitLogFactory.createByConfig();

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig, topicService);
        commitLogFacade = new CommitLogFacade(commitConfig, partitioner);
        commitLogFacade.addCommitLog(logList);

        context.register(partitioner);
        context.register(commitLogFacade);
    }

    private void registerAPI() {
        CommitLogStore api = new CommitLogStoreImpl(commitLogFacade);
        context.register(api, CommitLogStore.class, ContextNamespace.API);
    }
}
