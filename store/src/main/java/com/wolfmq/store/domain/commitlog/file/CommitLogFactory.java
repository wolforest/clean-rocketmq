package com.wolfmq.store.domain.commitlog.file;

import cn.coderule.common.util.io.DirUtil;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.store.domain.commitlog.flush.handler.FlushHandler;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.store.domain.commitlog.flush.handler.DefaultFlushHandler;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import com.wolfmq.store.infra.file.DefaultMappedFileQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommitLogFactory {
    private final StoreConfig storeConfig;
    private final CommitConfig commitConfig;

    private final int shardNumber;
    private final QueueProgress queueProgress;
    private final AllocateMappedFileService allocateService;

    public CommitLogFactory(
        StoreConfig storeConfig,
        QueueProgress queueProgress,
        AllocateMappedFileService allocateService
    ) {
        this.storeConfig = storeConfig;
        this.commitConfig = storeConfig.getCommit();
        this.shardNumber = commitConfig.getShardingNumber();

        this.queueProgress = queueProgress;
        this.allocateService = allocateService;
    }

    public List<CommitLog> createByConfig() {
        List<CommitLog> logList = new ArrayList<>();
        for (int i = 0; i < shardNumber; i++) {
            logList.add(createByShardId(i));
        }

        return logList;
    }

    public CommitLog createByShardId(int shardId) {
        MappedFileQueue fileQueue = initMappedFileQueue(shardId);
        FlushHandler flushPolicy = createFlushPolicy(fileQueue);
        return new FileCommitLog(storeConfig, shardId, fileQueue, flushPolicy, queueProgress);
    }

    private DefaultMappedFileQueue initMappedFileQueue(int shardId) {
        String dir = storeConfig.getPath().getRootPath()
            + File.separator
            + commitConfig.getDirName()
            + File.separator
            + shardId
            + File.separator;
        DirUtil.createIfNotExists(dir);

        return new DefaultMappedFileQueue(dir, commitConfig.getFileSize(), allocateService);
    }

    private FlushHandler createFlushPolicy(MappedFileQueue mappedFileQueue) {
        return new DefaultFlushHandler(commitConfig, mappedFileQueue, queueProgress);
    }


}
