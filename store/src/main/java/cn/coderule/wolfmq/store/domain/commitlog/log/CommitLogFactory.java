package cn.coderule.wolfmq.store.domain.commitlog.log;

import cn.coderule.common.util.io.DirUtil;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLogFlushPolicy;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import cn.coderule.wolfmq.store.domain.commitlog.flush.policy.DefaultCommitLogFlushPolicy;
import cn.coderule.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.wolfmq.store.infra.file.DefaultMappedFileQueue;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommitLogFactory {
    private final StoreConfig storeConfig;
    private final CommitConfig commitConfig;

    private final int shardNumber;
    private final CheckPoint checkpoint;

    public CommitLogFactory(StoreConfig storeConfig, CheckPoint checkpoint) {
        this.storeConfig = storeConfig;
        this.commitConfig = storeConfig.getCommitConfig();
        this.shardNumber = commitConfig.getShardingNumber();

        this.checkpoint = checkpoint;
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
        CommitLogFlushPolicy flushPolicy = initFlushPolicy(shardId, fileQueue);
        return new DefaultCommitLog(storeConfig, shardId, fileQueue, flushPolicy, checkpoint);
    }

    private DefaultMappedFileQueue initMappedFileQueue(int shardId) {
        String dir = storeConfig.getRootDir()
            + File.separator
            + commitConfig.getDirName()
            + File.separator
            + shardId
            + File.separator;
        DirUtil.createIfNotExists(dir);

        AllocateMappedFileService allocateService = StoreContext.getBean(AllocateMappedFileService.class);
        return new DefaultMappedFileQueue(dir, commitConfig.getFileSize(), allocateService);
    }

    private CommitLogFlushPolicy initFlushPolicy(int shardId, MappedFileQueue mappedFileQueue) {
        return new DefaultCommitLogFlushPolicy(shardId, commitConfig, mappedFileQueue, checkpoint);
    }


}
