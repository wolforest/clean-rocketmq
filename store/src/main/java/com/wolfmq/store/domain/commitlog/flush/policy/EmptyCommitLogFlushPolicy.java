package com.wolfmq.store.domain.commitlog.flush.policy;

import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.commitlog.flush.CommitLogFlushPolicy;
import com.wolfmq.common.domain.mq.dto.EnqueueFuture;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * simply sync commit log
 * for testing
 */
@Slf4j
public class EmptyCommitLogFlushPolicy implements CommitLogFlushPolicy {
    private static final int RETRY_TIMES = 10;

    private final MappedFileQueue mappedFileQueue;

    public EmptyCommitLogFlushPolicy(MappedFileQueue mappedFileQueue) {
        this.mappedFileQueue = mappedFileQueue;
    }

    @Override
    public EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO) {
        return EnqueueFuture.success(insertResult, messageBO);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
