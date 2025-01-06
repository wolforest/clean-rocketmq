package com.wolf.minimq.store.domain.commitlog;

import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.config.StoreConfig;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.domain.service.store.manager.CommitLogManager;
import com.wolf.minimq.store.domain.commitlog.flush.FlushManager;
import com.wolf.minimq.store.infra.file.AllocateMappedFileService;
import com.wolf.minimq.store.infra.file.DefaultMappedFileQueue;
import com.wolf.minimq.store.infra.memory.CLibrary;
import com.wolf.minimq.store.server.StoreCheckpoint;
import com.wolf.minimq.store.server.StoreContext;
import java.io.File;

/**
 * depend on:
 *  - StoreConfig
 *  - CommitLogConfig
 */
public class DefaultCommitLogManager implements CommitLogManager {
    private StoreConfig storeConfig;
    private CommitLogConfig commitLogConfig;
    private MessageConfig messageConfig;

    private MappedFileQueue mappedFileQueue;
    private final FlushManager flushManager;

    public DefaultCommitLogManager() {
        initConfig();
        initMappedFileQueue();

        StoreCheckpoint checkpoint = StoreContext.getBean(StoreCheckpoint.class);
        flushManager = new FlushManager(commitLogConfig, mappedFileQueue, checkpoint);

        CommitLog commitLog = new DefaultCommitLog(commitLogConfig, messageConfig, mappedFileQueue, flushManager);
        StoreContext.register(commitLog, CommitLog.class);
    }

    @Override
    public void initialize() {
        mappedFileQueue.load();
        mappedFileQueue.setFileMode(CLibrary.MADV_RANDOM);
        mappedFileQueue.checkSelf();
    }

    @Override
    public void start() {
        flushManager.start();
    }

    @Override
    public void shutdown() {
        flushManager.shutdown();
    }

    @Override
    public void cleanup() {
        flushManager.cleanup();
    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initConfig() {
        storeConfig = StoreContext.getBean(StoreConfig.class);
        commitLogConfig = StoreContext.getBean(CommitLogConfig.class);
        messageConfig = StoreContext.getBean(MessageConfig.class);
    }

    private void initMappedFileQueue() {
        String dir = storeConfig.getRootDir()
            + File.separator
            + commitLogConfig.getDirName()
            + File.separator;

        AllocateMappedFileService allocateService = StoreContext.getBean(AllocateMappedFileService.class);
        this.mappedFileQueue = new DefaultMappedFileQueue(dir, commitLogConfig.getFileSize(), allocateService);
    }
}
