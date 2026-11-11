package com.wolfmq.store.domain.commitlog.it;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.server.store.api.CommitLogStore;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.offset.checkpoint.bakeup.CheckPoint;
import com.wolfmq.common.testing.ConfigMock;
import com.wolfmq.store.api.CommitLogStoreImpl;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.common.domain.offset.DefaultQueueProgress;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import com.wolfmq.common.domain.offset.consumequeue.QueueOffsetAssigner;
import com.wolfmq.common.domain.offset.checkpoint.store.FileCheckpointStore;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.store.domain.commitlog.file.CommitLogManager;
import com.wolfmq.store.domain.commitlog.file.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.file.FileCommitLog;
import com.wolfmq.store.domain.commitlog.route.TopicPartitioner;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;

public abstract class BaseCommitLogIntegrationTest {
    protected ApplicationContext context = new ApplicationContext();


    protected static final int MMAP_FILE_SIZE = 1024 * 1024;

    @TempDir
    protected Path tmpDir;

    protected StoreConfig storeConfig;
    protected AllocateMappedFileService allocateService;
    protected CommitLogManager logManager;
    protected CommitLogFacade commitLogFacade;
    protected CommitLogStore commitLogStore;
    protected TopicPartitioner partitioner;

    @BeforeEach
    void setUpInfrastructure() throws Exception {
        resetStoreContext();

        String rootPath = tmpDir.toString();
        storeConfig = ConfigMock.createStoreConfig(rootPath);
        storeConfig.getCommit().setFileSize(MMAP_FILE_SIZE);
        storeConfig.getCommit().setShardingNumber(1);
        storeConfig.getCommit().setMaxShardingNumber(1);

        context.register(storeConfig);
        context.register(storeConfig.getCommit());

        initCheckPoint();

        allocateService = new AllocateMappedFileService(storeConfig);
        allocateService.start();
        context.register(allocateService);

        initCommitLog();
    }

    private void resetStoreContext() {
    }

    @AfterEach
    void tearDownInfrastructure() throws Exception {
        if (logManager != null) {
            logManager.shutdown();
        }
        if (allocateService != null) {
            allocateService.shutdown();
        }
        for (int shardId = 0; shardId < maxShard(); shardId++) {
            try {
                CommitLog cl = logManager.selectByShard(shardId);
                if (cl != null) {
                    cl.destroy();
                }
            } catch (Exception ignore) {
            }
        }
    }

    private int maxShard() {
        return storeConfig.getCommit().getShardingNumber();
    }

    private void initCheckPoint() {
        StoreCheckpoint checkpoint = new StoreCheckpoint(
            tmpDir.resolve("checkpoint").toString()
        );
        checkpoint.setShutdownSuccessful(false);
        context.register(checkpoint, CheckPoint.class);
    }

    private void initCommitLog() {
        QueueProgress queueProgress = createQueueProgress();
        CommitConfig commitConfig = storeConfig.getCommit();

        CommitLogFactory factory = new CommitLogFactory(storeConfig, queueProgress, allocateService);

        partitioner = new TopicPartitioner(commitConfig);
        logManager = new CommitLogManager(commitConfig, factory);
        commitLogFacade = new CommitLogFacade(logManager);

        context.register(partitioner);
        context.register(logManager);
        context.register(commitLogFacade);

        commitLogStore = new CommitLogStoreImpl(commitLogFacade);

        try {
            logManager.initialize();
            logManager.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        ensureMappedFileExists();
    }

    private void ensureMappedFileExists() {
        CommitLog cl = logManager.selectByShard(0);
        ((FileCommitLog) cl).getMappedFileQueue().getOrCreateMappedFileForSize(100);
    }

    private QueueProgress createQueueProgress() {
        FileCheckpointStore store = new FileCheckpointStore(
            tmpDir.resolve("queue-progress").toString()
        );

        OffsetCodec offsetCodec = new OffsetCodec(storeConfig.getCommit().getMaxShardingNumber());
        QueueOffsetAssigner assigner = new QueueOffsetAssigner(store);

        return new DefaultQueueProgress(store, offsetCodec, assigner);
    }
}
