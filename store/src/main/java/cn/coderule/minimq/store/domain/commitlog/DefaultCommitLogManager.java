package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.config.CommitConfig;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogManager;
import cn.coderule.minimq.domain.service.store.server.CheckPoint;
import cn.coderule.minimq.store.api.CommitLogStoreImpl;
import cn.coderule.minimq.store.domain.commitlog.flush.FlushManager;
import cn.coderule.minimq.store.infra.file.AllocateMappedFileService;
import cn.coderule.minimq.store.infra.file.DefaultMappedFileQueue;
import cn.coderule.minimq.store.infra.memory.CLibrary;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import java.io.File;

/**
 * depend on:
 *  - StoreConfig
 *  - CommitLogConfig
 */
public class DefaultCommitLogManager implements CommitLogManager {
    private StoreConfig storeConfig;
    private CommitConfig commitConfig;
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
        commitConfig = StoreContext.getBean(CommitConfig.class);
        messageConfig = StoreContext.getBean(MessageConfig.class);
    }

    private void initMappedFileQueue() {
        String dir = storeConfig.getRootDir()
            + File.separator
            + commitConfig.getDirName()
            + File.separator;

        AllocateMappedFileService allocateService = StoreContext.getBean(AllocateMappedFileService.class);
        this.mappedFileQueue = new DefaultMappedFileQueue(dir, commitConfig.getFileSize(), allocateService);
    }

    private void initCommitLog() {
        checkpoint = StoreContext.getCheckPoint();
        flushManager = new FlushManager(commitConfig, mappedFileQueue, checkpoint);

        commitLog = new DefaultCommitLog(commitConfig, messageConfig, mappedFileQueue, flushManager);
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
        CommitLogStore api = new CommitLogStoreImpl(commitLog);
        StoreContext.registerAPI(api, CommitLogStore.class);
    }

}
