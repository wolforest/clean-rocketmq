package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.api.CommitLogStore;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLogManager;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import cn.coderule.minimq.store.api.CommitLogStoreImpl;
import cn.coderule.minimq.store.domain.commitlog.flush.DefaultCommitLogFlusher;
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
    private DefaultCommitLogFlusher defaultCommitFlusher;
    private CheckPoint checkpoint;

    @Override
    public void initialize() throws Exception {
        initConfig();
        initMappedFileQueue();
        initCommitLog();

        load();
        recover();
        registerAPI();
    }

    @Override
    public void start() throws Exception {
        defaultCommitFlusher.start();
    }

    @Override
    public void shutdown() throws Exception {
        defaultCommitFlusher.shutdown();
    }

    @Override
    public void cleanup() throws Exception {
        defaultCommitFlusher.cleanup();
    }

    private void initConfig() {
        storeConfig = StoreContext.getBean(StoreConfig.class);
        commitConfig = storeConfig.getCommitConfig();
        messageConfig = storeConfig.getMessageConfig();
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
        defaultCommitFlusher = new DefaultCommitLogFlusher(commitConfig, mappedFileQueue, checkpoint);

        commitLog = new DefaultCommitLog(storeConfig, mappedFileQueue, defaultCommitFlusher);
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
