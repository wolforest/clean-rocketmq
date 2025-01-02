package com.wolf.minimq.store.domain.commitlog;

import com.wolf.common.util.encrypt.HashUtil;
import com.wolf.common.util.lang.SystemUtil;
import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.enums.MessageVersion;
import com.wolf.minimq.domain.exception.InvalidMessageException;
import com.wolf.minimq.domain.model.dto.InsertResult;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.utils.MessageEncoder;
import com.wolf.minimq.domain.utils.lock.CommitLogLock;
import com.wolf.minimq.domain.utils.lock.CommitLogReentrantLock;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.store.domain.commitlog.flush.FlushManager;
import com.wolf.minimq.store.domain.commitlog.vo.EnqueueThreadLocal;
import com.wolf.minimq.store.domain.commitlog.vo.InsertContext;
import com.wolf.minimq.store.infra.memory.CLibrary;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<EnqueueResult> insert(MessageBO messageBO) {
        InsertContext context = initContext(messageBO);
        MessageEncoder encoder = context.getEncoder();
        MappedFile mappedFile = context.getMappedFile();

        this.lock.lock();
        try {
            long commitLogOffset = mappedFile.getOffsetInFileName() + mappedFile.getWritePosition();
            messageBO.setCommitLogOffset(commitLogOffset);

            InsertResult insertResult = mappedFile.insert(encoder.encode());
            CompletableFuture<EnqueueResult> insertError = formatInsertResult(insertResult, context);
            if (insertError != null) {
                return insertError;
            }

            return handleFlush(insertResult, context);
        } catch (InvalidMessageException messageException) {
            return CompletableFuture.completedFuture(new EnqueueResult(messageException.getStatus()));
        } finally {
            this.lock.unlock();
        }
    }

    private CompletableFuture<EnqueueResult> handleFlush(InsertResult insertResult, InsertContext context) {
        return null;
    }

    private CompletableFuture<EnqueueResult> formatInsertResult(InsertResult insertResult, InsertContext context) {
        return null;
    }

    private InsertContext initContext(MessageBO messageBO) {
        initMessage(messageBO);

        long now = System.currentTimeMillis();
        messageBO.setStoreTimestamp(now);

        MessageEncoder encoder = localEncoder.get().getEncoder(messageBO);
        MappedFile mappedFile = mappedFileQueue.getAvailableMappedFile(encoder.getMessageLength());
        mappedFile.setFileMode(CLibrary.MADV_RANDOM);

        return InsertContext.builder()
            .now(now)
            .messageBO(messageBO)
            .encoder(encoder)
            .mappedFile(mappedFile)
            .build();
    }

    private void initMessage(MessageBO messageBO) {
        messageBO.setBodyCRC(HashUtil.crc32(messageBO.getBody()));

        messageBO.setVersion(MessageVersion.V1);
        if (messageBO.getTopic().length() > Byte.MAX_VALUE) {
            messageBO.setVersion(MessageVersion.V2);
        }
    }

    @Override
    public MessageBO select(long offset) {
        return null;
    }

    @Override
    public MessageBO select(long offset, int size) {
        return null;
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
    public long getFlushedOffset() {
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
