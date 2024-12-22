package com.wolf.minimq.store.domain.commitlog;

import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.lock.CommitLogLock;
import com.wolf.minimq.domain.lock.CommitLogReentrantLock;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.domain.vo.EnqueueResult;
import com.wolf.minimq.domain.vo.MessageContext;
import com.wolf.minimq.domain.vo.SelectedMappedBuffer;
import com.wolf.minimq.store.domain.commitlog.flush.FlushManager;
import java.util.List;

/**
 * depend on:
 *  - CommitLogConfig
 *  - MappedFileQueue
 *  - FlushManager
 */
public class DefaultCommitLog implements CommitLog {
    private final CommitLogConfig commitLogConfig;
    private final MappedFileQueue mappedFileQueue;
    private final FlushManager flushManager;

    private final CommitLogLock lock;

    public DefaultCommitLog(
        CommitLogConfig commitLogConfig,
        MappedFileQueue mappedFileQueue,
        FlushManager flushManager
    ) {
        this.commitLogConfig = commitLogConfig;
        this.mappedFileQueue = mappedFileQueue;
        this.flushManager = flushManager;

        this.lock = new CommitLogReentrantLock();
    }

    @Override
    public EnqueueResult append(MessageContext messageContext) {
        return null;
    }

    @Override
    public SelectedMappedBuffer select(long offset, int size) {
        return null;
    }

    @Override
    public SelectedMappedBuffer select(long offset) {
        return null;
    }

    @Override
    public List<SelectedMappedBuffer> selectAll(long offset, int size) {
        return List.of();
    }
}
