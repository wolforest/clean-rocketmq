package com.wolfmq.store.domain.commitlog.flush.policy;

import com.wolfmq.domain.domain.mq.dto.EnqueueFuture;
import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.commitlog.flush.CommitLogFlushPolicy;
import com.wolfmq.domain.core.mmap.MappedFileQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * simply sync commit log
 * for testing
 */
@Slf4j
public class SyncCommitLogFlushPolicy implements CommitLogFlushPolicy {
    private static final int RETRY_TIMES = 10;

    private final MappedFileQueue mappedFileQueue;

    public SyncCommitLogFlushPolicy(MappedFileQueue mappedFileQueue) {
        this.mappedFileQueue = mappedFileQueue;
    }

    @Override
    public EnqueueFuture flush(InsertResult insertResult, MessageBO messageBO) {
        boolean status = forceFlush();
        if (!status) {
            return EnqueueFuture.failure();
        }

        return EnqueueFuture.success(insertResult, messageBO);
    }

    private boolean forceFlush() {
        boolean result = false;
        for (int i = 0; i < RETRY_TIMES && !result; i++) {
            result = mappedFileQueue.flush(0);
            String status = result ? "OK" : "Not OK";
            log.info("SyncCommitLogFlusher shutdown, retry {} times {}", i + 1, status);
        }

        return result;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
