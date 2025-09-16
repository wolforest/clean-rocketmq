package cn.coderule.minimq.domain.domain.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;

public interface CommitLogFlusher {
    EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO);
}
