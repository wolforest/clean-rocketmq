package cn.coderule.wolfmq.store.domain.commitlog.flush.policy;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLogFlushPolicy;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
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
