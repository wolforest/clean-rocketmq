package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;

public interface CommitFlusher {
    InsertFuture flush(InsertResult insertResult, MessageBO messageBO);
}
