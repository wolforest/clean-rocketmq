package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogFlusher;

/**
 * simply sync commit log
 * for testing
 */
public class SyncCommitLogFlusher implements CommitLogFlusher {
    @Override
    public InsertFuture flush(InsertResult insertResult, MessageBO messageBO) {
        return null;
    }
}
