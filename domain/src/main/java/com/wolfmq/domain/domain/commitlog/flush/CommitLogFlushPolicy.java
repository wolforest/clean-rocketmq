package com.wolfmq.domain.domain.commitlog.flush;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.domain.mq.dto.EnqueueFuture;
import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.domain.domain.message.MessageBO;

/**
 * @renamed from CommitLogFlusher to CommitLogFlushPolicy
 */
public interface CommitLogFlushPolicy extends Lifecycle {
    EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO);
}
