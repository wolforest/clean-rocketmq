package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.api.CommitLogStore;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import cn.coderule.minimq.store.api.CommitLogStoreImpl;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
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

        CommitLogFactory commitLogFactory = new CommitLogFactory(storeConfig, checkpoint);
        List<CommitLog> logList = commitLogFactory.createByConfig();

        commitLogManager = new CommitLogManager(storeConfig.getCommitConfig());
        commitLogManager.addCommitLog(logList);
    }

    private void registerAPI() {
        CommitLogStore api = new CommitLogStoreImpl(commitLogManager);
        StoreContext.registerAPI(api, CommitLogStore.class);
    }
}
