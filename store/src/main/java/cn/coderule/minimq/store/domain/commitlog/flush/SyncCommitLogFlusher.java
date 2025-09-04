package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogFlusher;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;

/**
 * simply sync commit log
 * for testing
 */
public class SyncCommitLogFlusher implements CommitLogFlusher {
    private final StoreConfig storeConfig;
    private final MappedFileQueue mappedFileQueue;

    public SyncCommitLogFlusher(
        StoreConfig storeConfig,
        MappedFileQueue mappedFileQueue
    ) {
        this.storeConfig = storeConfig;
        this.mappedFileQueue = mappedFileQueue;
    }

    @Override
    public InsertFuture flush(InsertResult insertResult, MessageBO messageBO) {
        return null;
    }
}
