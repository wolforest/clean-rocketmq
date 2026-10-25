package cn.coderule.wolfmq.domain.domain.commitlog;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.domain.mq.dto.EnqueueFuture;
import cn.coderule.wolfmq.domain.core.mmap.InsertResult;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;

/**
 * @renamed from CommitLogFlusher to CommitLogFlushPolicy
 */
public interface CommitLogFlushPolicy extends Lifecycle {
    EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO);
}
