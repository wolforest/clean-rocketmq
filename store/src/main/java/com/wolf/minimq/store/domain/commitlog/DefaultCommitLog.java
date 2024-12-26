package com.wolf.minimq.store.domain.commitlog;

import com.wolf.common.util.encrypt.HashUtil;
import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.enums.MessageVersion;
import com.wolf.minimq.domain.utils.lock.CommitLogLock;
import com.wolf.minimq.domain.utils.lock.CommitLogReentrantLock;
import com.wolf.minimq.domain.model.Message;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.domain.model.vo.EnqueueResult;
import com.wolf.minimq.domain.model.vo.MessageContext;
import com.wolf.minimq.domain.model.vo.SelectedMappedBuffer;
import com.wolf.minimq.store.domain.commitlog.flush.FlushManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;

/**
 * depend on:
 *  - CommitLogConfig
 *  - MappedFileQueue
 *  - FlushManager
 */
public class DefaultCommitLog implements CommitLog {
    private final CommitLogConfig commitLogConfig;
    private final MessageConfig messageConfig;
    private final MappedFileQueue mappedFileQueue;
    private final FlushManager flushManager;

    private final CommitLogLock lock;
    private ThreadLocal<EnqueueThreadLocal> localEncoder;
    @Getter @Setter
    private volatile long confirmOffset = -1L;

    public DefaultCommitLog(
        CommitLogConfig commitLogConfig,
        MessageConfig messageConfig,
        MappedFileQueue mappedFileQueue,
        FlushManager flushManager
    ) {
        this.commitLogConfig = commitLogConfig;
        this.messageConfig = messageConfig;

        this.mappedFileQueue = mappedFileQueue;
        this.flushManager = flushManager;

        this.lock = new CommitLogReentrantLock();
        initLocalEncoder();
    }

    @Override
    public CompletableFuture<EnqueueResult> insert(MessageContext messageContext) {
        initAppendMessage(messageContext);

        return null;
    }

    private void initAppendMessage(MessageContext context) {
        Message message = context.getMessage();

        context.setStoreTimestamp(System.currentTimeMillis());
        context.setBodyCRC(HashUtil.crc32(message.getBody()));

        context.setVersion(MessageVersion.V1);
        if (message.getTopic().length() > Byte.MAX_VALUE) {
            context.setVersion(MessageVersion.V2);
        }
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

    @Override
    public long getMinOffset() {
        return mappedFileQueue.getMinOffset();
    }

    @Override
    public long getMaxOffset() {
        return mappedFileQueue.getMaxOffset();
    }

    @Override
    public long getFlushPosition() {
        return mappedFileQueue.getFlushPosition();
    }

    @Override
    public long getUnFlushedSize() {
        return mappedFileQueue.getUnFlushedSize();
    }

    private void initLocalEncoder() {
        localEncoder = ThreadLocal.withInitial(
            () -> new EnqueueThreadLocal(messageConfig)
        );
    }
}
