package com.wolfmq.store.domain.commitlog;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.server.store.api.CommitLogStore;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.store.api.CommitLogStoreImpl;
import com.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.log.CommitLogProxy;
import com.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import java.util.List;

/**
 * depend on:
 *  - StoreConfig
 *  - CommitLogConfig
 */
public class CommitLogBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private CommitLogProxy commitLogProxy;

    public CommitLogBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        initCommitLog();
        commitLogProxy.initialize();

        registerAPI();
    }

    @Override
    public void start() throws Exception {
        commitLogProxy.start();
    }

    @Override
    public void shutdown() throws Exception {
        commitLogProxy.shutdown();
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
        commitLogProxy = new CommitLogProxy(commitConfig, partitioner);
        commitLogProxy.addCommitLog(logList);

        context.register(partitioner);
        context.register(commitLogProxy);
    }

    private void registerAPI() {
        CommitLogStore api = new CommitLogStoreImpl(commitLogProxy);
        context.register(api, CommitLogStore.class, ContextNamespace.API);
    }
}
