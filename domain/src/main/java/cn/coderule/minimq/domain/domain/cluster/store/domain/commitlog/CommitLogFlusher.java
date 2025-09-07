package cn.coderule.minimq.domain.domain.cluster.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;

public interface CommitLogFlusher {
    InsertFuture flush(InsertResult insertResult, MessageBO messageBO);
}
