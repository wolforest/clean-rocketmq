package cn.coderule.wolfmq.store.domain.commitlog;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.api.CommitLogStore;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import cn.coderule.wolfmq.store.api.CommitLogStoreImpl;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
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
