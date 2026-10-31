package com.wolfmq.store.domain.dispatcher.it;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.server.store.api.CommitLogStore;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.domain.test.ConfigMock;
import com.wolfmq.store.api.CommitLogStoreImpl;
import com.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.CommitLogProxy;
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
    protected CommitLogProxy commitLogProxy;
    protected CommitLogStore commitLogStore;
    protected CommitHandlerManager handlerManager;
    protected DispatchQueue dispatchQueue;

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

        handlerManager = new CommitHandlerManager();
        dispatchQueue = new DispatchQueue(storeConfig.getCommit());
    }

    @AfterEach
    void tearDownInfrastructure() throws Exception {
        if (commitLogProxy != null) {
            commitLogProxy.shutdown();
        }
        if (allocateService != null) {
            allocateService.shutdown();
        }
        for (int shardId = 0; shardId < maxShard(); shardId++) {
            try {
                CommitLog cl = commitLogProxy.selectByShardId(shardId);
                if (cl != null) {
                    cl.destroy();
                }
            } catch (Exception ignore) {
            }
        }
        resetStoreContext();
    }

    private int maxShard() {
        return storeConfig.getCommit().getShardingNumber();
    }

    private void resetStoreContext() {
    }

    private void initCheckPoint() {
        StoreCheckpoint checkpoint = new StoreCheckpoint(
            tmpDir.resolve("checkpoint").toString()
        );
        checkpoint.setShutdownSuccessful(false);
        context.register(checkpoint, CheckPoint.class);
    }

    private void initCommitLog() throws Exception {
        CheckPoint checkpoint = context.getBean(CheckPoint.class);
        CommitConfig commitConfig = storeConfig.getCommit();

        CommitLogFactory factory = new CommitLogFactory(storeConfig, checkpoint, allocateService);
        List<CommitLog> logList = factory.createByConfig();

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig);
        commitLogProxy = new CommitLogProxy(commitConfig, partitioner);
        commitLogProxy.addCommitLog(logList);

        context.register(partitioner);
        context.register(commitLogProxy);

        commitLogStore = new CommitLogStoreImpl(commitLogProxy);

        commitLogProxy.initialize();
        commitLogProxy.start();

        ensureMappedFileExists();
    }

    private void ensureMappedFileExists() {
        CommitLog cl = commitLogProxy.selectByShardId(0);
        cl.getMappedFileQueue().getOrCreateMappedFileForSize(100);
    }

    protected CommitListener createCommitListener() {
        CommitLog commitLog = commitLogProxy.selectByShardId(0);
        return new CommitListener(dispatchQueue, commitLog, context.getBean(CheckPoint.class), storeConfig.getCommit());
    }

    protected AtomicInteger registerCountingHandler() {
        AtomicInteger counter = new AtomicInteger(0);
        handlerManager.registerHandler(event -> counter.incrementAndGet());
        return counter;
    }
}
