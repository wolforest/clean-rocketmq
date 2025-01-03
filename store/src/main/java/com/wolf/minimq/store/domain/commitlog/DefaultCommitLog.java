package com.wolf.minimq.store.domain.commitlog;

import com.wolf.common.util.encrypt.HashUtil;
import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.domain.enums.MessageVersion;
import com.wolf.minimq.domain.exception.EnqueueErrorException;
import com.wolf.minimq.domain.model.dto.InsertResult;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.utils.MessageDecoder;
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

    private final CommitLogLock commitLogLock;
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

        this.commitLogLock = new CommitLogReentrantLock();
        initLocalEncoder();
    }

    @Override
    public CompletableFuture<EnqueueResult> insert(MessageBO messageBO) {
        InsertContext context = initContext(messageBO);

        this.commitLogLock.lock();
        try {
            MappedFile mappedFile = getMappedFile(context.getEncoder());
            assignOffset(messageBO, mappedFile);

            InsertResult insertResult = mappedFile.insert(context.getEncoder().encode());
            handleInsertError(insertResult);

            return handleFlush(insertResult, context);
        } catch (EnqueueErrorException messageException) {
            return CompletableFuture.completedFuture(new EnqueueResult(messageException.getStatus()));
        } finally {
            this.commitLogLock.unlock();
        }
    }

    @Override
    public MessageBO select(long offset, int size) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int) (offset % commitLogConfig.getFileSize());
        SelectedMappedBuffer buffer = mappedFile.select(position, size);
        if (buffer == null) {
            return null;
        }

        return MessageDecoder.decode(buffer.getByteBuffer());
    }

    @Override
    public MessageBO select(long offset) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int) (offset % commitLogConfig.getFileSize());
        SelectedMappedBuffer buffer = mappedFile.select(position);
        if (buffer == null) {
            return null;
        }

        int size = buffer.getByteBuffer().getInt();
        buffer.getByteBuffer().limit(size);

        return MessageDecoder.decode(buffer.getByteBuffer());
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

    private InsertContext initContext(MessageBO messageBO) {
        initMessage(messageBO);

        long now = System.currentTimeMillis();
        messageBO.setStoreTimestamp(now);

        return InsertContext.builder()
            .now(now)
            .messageBO(messageBO)
            .encoder(localEncoder.get().getEncoder(messageBO))
            .build();
    }

    private void initMessage(MessageBO messageBO) {
        messageBO.setBodyCRC(HashUtil.crc32(messageBO.getBody()));

        messageBO.setVersion(MessageVersion.V1);
        if (messageBO.getTopic().length() > Byte.MAX_VALUE) {
            messageBO.setVersion(MessageVersion.V2);
        }
    }

    private MappedFile getMappedFile(MessageEncoder encoder) {
        MappedFile mappedFile = mappedFileQueue.getAvailableMappedFile(encoder.getMessageLength());
        mappedFile.setFileMode(CLibrary.MADV_RANDOM);

        return mappedFile;
    }

    private void assignOffset(MessageBO messageBO, MappedFile mappedFile) {
        long commitLogOffset = mappedFile.getOffsetInFileName() + mappedFile.getWritePosition();
        messageBO.setCommitLogOffset(commitLogOffset);
    }

    private void handleInsertError(InsertResult insertResult) {
        switch (insertResult.getStatus()) {
            case END_OF_FILE -> throw new EnqueueErrorException(EnqueueStatus.END_OF_FILE);
            case MESSAGE_SIZE_EXCEEDED, PROPERTIES_SIZE_EXCEEDED -> throw new EnqueueErrorException(EnqueueStatus.MESSAGE_ILLEGAL);
            default -> throw new EnqueueErrorException(EnqueueStatus.UNKNOWN_ERROR);
        }
    }

    private CompletableFuture<EnqueueResult> handleFlush(InsertResult insertResult, InsertContext context) {
        EnqueueResult enqueueResult = EnqueueResult.builder()
            .insertResult(insertResult)
            .build();
        return flushManager.flush(enqueueResult, context.getMessageBO());
    }


}
