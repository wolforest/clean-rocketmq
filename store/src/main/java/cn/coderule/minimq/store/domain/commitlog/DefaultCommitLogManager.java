package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.config.CommitLogConfig;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.model.checkpoint.CheckPoint;
import cn.coderule.minimq.domain.service.store.api.CommitLogService;
import cn.coderule.minimq.domain.service.store.domain.CommitLog;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.service.store.manager.CommitLogManager;
import cn.coderule.minimq.store.api.CommitLogServiceImpl;
import cn.coderule.minimq.store.domain.commitlog.flush.FlushManager;
import cn.coderule.minimq.store.infra.file.AllocateMappedFileService;
import cn.coderule.minimq.store.infra.file.DefaultMappedFileQueue;
import cn.coderule.minimq.store.infra.memory.CLibrary;
import cn.coderule.minimq.store.server.StoreCheckpoint;
import cn.coderule.minimq.store.server.StoreContext;
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
    private CommitLog commitLog;
    private FlushManager flushManager;
    private CheckPoint checkpoint;

    @Override
    public void initialize() {
        initConfig();
        initMappedFileQueue();
        initCommitLog();

        load();
        recover();
        registerAPI();
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

    private void initCommitLog() {
        checkpoint = StoreContext.getBean(StoreCheckpoint.class);
        flushManager = new FlushManager(commitLogConfig, mappedFileQueue, checkpoint);

        commitLog = new DefaultCommitLog(commitLogConfig, messageConfig, mappedFileQueue, flushManager);
        StoreContext.register(commitLog, CommitLog.class);
    }

    private void load() {
        mappedFileQueue.load();
        mappedFileQueue.setFileMode(CLibrary.MADV_RANDOM);
        mappedFileQueue.checkSelf();
    }

    private void recover() {
        CommitLogRecovery commitLogRecovery = new CommitLogRecovery(commitLog, checkpoint);
        commitLogRecovery.recover();
    }

    private void registerAPI() {
        CommitLogService api = new CommitLogServiceImpl(commitLog);
        StoreContext.registerAPI(api, CommitLogService.class);
    }

}
