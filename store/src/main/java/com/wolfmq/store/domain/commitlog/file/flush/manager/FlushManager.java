package com.wolfmq.store.domain.commitlog.file.flush.manager;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueFuture;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.domain.message.MessageBO;

/**
 * @renamed from CommitLogFlusher to CommitLogFlushPolicy
 * @renamed from FlushHandler to FlushManager
 */
public interface FlushManager extends Lifecycle {
    EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO);
}
