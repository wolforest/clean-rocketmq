package com.wolfmq.store.domain.dispatcher.it;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.server.store.api.CommitLogStore;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.api.CommitLogStoreImpl;
import com.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
import com.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import com.wolfmq.store.domain.dispatcher.CommitListener;
import com.wolfmq.store.domain.dispatcher.DispatchQueue;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseDispatcherIntegrationTest {
    protected ApplicationContext context = new ApplicationContext();


    protected static final int MMAP_FILE_SIZE = 1024 * 1024;

    @TempDir
    protected Path tmpDir;

    protected StoreConfig storeConfig;
    protected AllocateMappedFileService allocateService;
    protected CommitLogManager commitLogManager;
    protected CommitLogStore commitLogStore;
    protected CommitHandlerManager handlerManager;
    protected DispatchQueue dispatchQueue;

    @BeforeEach
    void setUpInfrastructure() throws Exception {
        resetStoreContext();

        String rootPath = tmpDir.toString();
        storeConfig = ConfigMock.createStoreConfig(rootPath);
        storeConfig.getCommitConfig().setFileSize(MMAP_FILE_SIZE);
        storeConfig.getCommitConfig().setShardingNumber(1);
        storeConfig.getCommitConfig().setMaxShardingNumber(1);

        context.register(storeConfig);
        context.register(storeConfig.getCommitConfig());

        initCheckPoint();

        allocateService = new AllocateMappedFileService(storeConfig);
        allocateService.start();
        context.register(allocateService);

        initCommitLog();

        handlerManager = new CommitHandlerManager();
        dispatchQueue = new DispatchQueue(storeConfig.getCommitConfig());
    }

    @AfterEach
    void tearDownInfrastructure() throws Exception {
        if (commitLogManager != null) {
            commitLogManager.shutdown();
        }
        if (allocateService != null) {
            allocateService.shutdown();
        }
        for (int shardId = 0; shardId < maxShard(); shardId++) {
            try {
                CommitLog cl = commitLogManager.selectByShardId(shardId);
                if (cl != null) {
                    cl.destroy();
                }
            } catch (Exception ignore) {
            }
        }
        resetStoreContext();
    }

    private int maxShard() {
        return storeConfig.getCommitConfig().getShardingNumber();
    }

    private void resetStoreContext() {
    }

    private void initCheckPoint() {
        StoreCheckpoint checkpoint = new StoreCheckpoint(
            tmpDir.resolve("checkpoint").toString()
        );
        checkpoint.setShutdownSuccessful(false);
        context.register(checkpoint, CheckPoint.class); context.register(checkpoint, StoreCheckpoint.class);
    }

    private void initCommitLog() throws Exception {
        CheckPoint checkpoint = context.getBean(StoreCheckpoint.class);
        CommitConfig commitConfig = storeConfig.getCommitConfig();

        CommitLogFactory factory = new CommitLogFactory(storeConfig, checkpoint, allocateService);
        List<CommitLog> logList = factory.createByConfig();

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig);
        commitLogManager = new CommitLogManager(commitConfig, partitioner);
        commitLogManager.addCommitLog(logList);

        context.register(partitioner);
        context.register(commitLogManager);

        commitLogStore = new CommitLogStoreImpl(commitLogManager);

        commitLogManager.initialize();
        commitLogManager.start();

        ensureMappedFileExists();
    }

    private void ensureMappedFileExists() {
        CommitLog cl = commitLogManager.selectByShardId(0);
        cl.getMappedFileQueue().getOrCreateMappedFileForSize(100);
    }

    protected CommitListener createCommitListener() {
        CommitLog commitLog = commitLogManager.selectByShardId(0);
        return new CommitListener(dispatchQueue, commitLog, context.getBean(StoreCheckpoint.class), storeConfig.getCommitConfig());
    }

    protected AtomicInteger registerCountingHandler() {
        AtomicInteger counter = new AtomicInteger(0);
        handlerManager.registerHandler(event -> counter.incrementAndGet());
        return counter;
    }
}
