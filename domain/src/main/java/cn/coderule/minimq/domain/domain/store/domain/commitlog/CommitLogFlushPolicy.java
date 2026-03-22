package cn.coderule.minimq.domain.domain.store.domain.commitlog;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;

/**
 * @renamed from CommitLogFlusher to CommitLogFlushPolicy
 */
public interface CommitLogFlushPolicy extends Lifecycle {
    EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO);
}
