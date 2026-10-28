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
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
import com.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import com.wolfmq.store.server.bootstrap.StoreContext;
import java.util.List;

/**
 * depend on:
 *  - StoreConfig
 *  - CommitLogConfig
 */
public class CommitLogBootstrap implements Lifecycle {
    private CommitLogManager commitLogManager;

    @Override
    public void initialize() throws Exception {
        initCommitLog();
        commitLogManager.initialize();

        registerAPI();
    }

    @Override
    public void start() throws Exception {
        commitLogManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        commitLogManager.shutdown();
    }

    private void initCommitLog() {
        CheckPoint checkpoint = StoreContext.getCheckPoint();
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        TopicService topicService = StoreContext.getBean(TopicService.class);

        CommitLogFactory commitLogFactory = new CommitLogFactory(storeConfig, checkpoint);
        List<CommitLog> logList = commitLogFactory.createByConfig();

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig, topicService);
        commitLogManager = new CommitLogManager(commitConfig, partitioner);
        commitLogManager.addCommitLog(logList);

        StoreContext.register(partitioner);
        StoreContext.register(commitLogManager);
    }

    private void registerAPI() {
        CommitLogStore api = new CommitLogStoreImpl(commitLogManager);
        StoreContext.registerAPI(api, CommitLogStore.class);
    }
}
