package com.wolfmq.store.domain.commitlog.flush.handler;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueFuture;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.domain.message.MessageBO;

/**
 * @renamed from CommitLogFlusher to CommitLogFlushPolicy
 */
public interface FlushHandler extends Lifecycle {
    EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO);
}
