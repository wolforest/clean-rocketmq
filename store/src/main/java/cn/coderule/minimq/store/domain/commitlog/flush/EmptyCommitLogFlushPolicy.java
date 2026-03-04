package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLogFlushPolicy;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
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
}
