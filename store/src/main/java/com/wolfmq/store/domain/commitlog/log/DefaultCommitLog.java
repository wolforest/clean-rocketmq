package com.wolfmq.store.domain.commitlog.log;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.core.constant.code.InvalidCode;
import com.wolfmq.domain.core.exception.InvalidRequestException;
import com.wolfmq.domain.domain.message.codec.MessageEncoder;
import com.wolfmq.domain.domain.commitlog.flush.CommitLogFlushPolicy;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.store.domain.commitlog.vo.InsertContext;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.domain.mq.dto.EnqueueStatus;
import com.wolfmq.domain.core.exception.EnqueueException;
import com.wolfmq.domain.domain.mq.dto.EnqueueFuture;
import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.domain.core.mmap.SelectedMappedBuffer;
import com.wolfmq.domain.core.mmap.MappedFile;
import com.wolfmq.domain.domain.message.codec.MessageDecoder;
import com.wolfmq.domain.core.lock.commitlog.CommitLogLock;
import com.wolfmq.domain.core.lock.commitlog.CommitLogReentrantLock;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.core.mmap.MappedFileQueue;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.store.domain.commitlog.vo.EnqueueThreadLocal;
import com.wolfmq.store.infra.memory.CLibrary;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * depend on:
 *  - CommitLogConfig
 *  - MappedFileQueue
 *  - FlushManager
 */
@Slf4j
public class DefaultCommitLog implements CommitLog {
    @Getter
    private final int shardId;

    private final StoreConfig storeConfig;
    private final CommitConfig commitConfig;
    private final MessageConfig messageConfig;

    private final CommitLogFlushPolicy flushPolicy;
    private final CheckPoint checkPoint;

    @Getter
    private final MappedFileQueue mappedFileQueue;
    private final CommitLogLock commitLogLock;
    private ThreadLocal<EnqueueThreadLocal> localEncoder;

    public DefaultCommitLog(
        StoreConfig storeConfig,
        int shardId,
        MappedFileQueue mappedFileQueue,
        CommitLogFlushPolicy flushPolicy
    ) {
        this(storeConfig, shardId, mappedFileQueue, flushPolicy, null);
    }

    public DefaultCommitLog(
        StoreConfig storeConfig,
        int shardId,
        MappedFileQueue mappedFileQueue,
        CommitLogFlushPolicy flushPolicy,
        CheckPoint checkPoint
    ) {
        this.shardId = shardId;
        this.storeConfig = storeConfig;
        this.commitConfig = storeConfig.getCommitConfig();
        this.messageConfig = storeConfig.getMessageConfig();

        this.checkPoint = checkPoint;
        this.mappedFileQueue = mappedFileQueue;
        this.flushPolicy = flushPolicy;

        this.commitLogLock = new CommitLogReentrantLock();
        initLocalEncoder();
    }

    @Override
    public void assignCommitOffset(MessageBO messageBO) {
        if (messageBO.getMessageLength() < 0) {
            throw new InvalidRequestException(
                InvalidCode.MESSAGE_CORRUPTED,
                "Message length can't be null, while assign commitOffset"
            );
        }

        MappedFile mappedFile = getMappedFile(messageBO.getMessageLength());
        assignCommitOffset(messageBO, mappedFile);
    }

    @Override
    public EnqueueFuture insert(MessageBO messageBO) {
        InsertContext context = initContext(messageBO);

        this.commitLogLock.lock();
        try {

            MappedFile mappedFile = getMappedFile(messageBO.getMessageLength());
            assignCommitOffset(messageBO, mappedFile);

            ByteBuffer messageBuffer = context.getEncoder().encode(messageBO);
            InsertResult insertResult = mappedFile.insert(messageBuffer);
            handleInsertError(insertResult);

            return flushPolicy.flush(insertResult, messageBO);
        } catch (EnqueueException messageException) {
            return EnqueueFuture.failure(messageException.getStatus());
        } finally {
            this.commitLogLock.unlock();
        }
    }

    @Override
    public InsertResult insert(long offset, byte[] data, int start, int size) {
        this.commitLogLock.lock();
        try {
            MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
            if (mappedFile == null) {
                return InsertResult.failure();
            }

            return mappedFile.insert(data, start, size);
        } catch (Exception e) {
            log.error("commitLog insert error", e);
            return InsertResult.failure();
        } finally {
            this.commitLogLock.unlock();
        }
    }

    @Override
    public MessageBO select(long offset, int size) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return MessageBO.notFound();
        }

        int position = (int) (offset % commitConfig.getFileSize());
        SelectedMappedBuffer buffer = mappedFile.select(position, size);
        if (buffer == null) {
            return MessageBO.notFound();
        }

        MessageBO messageBO = MessageDecoder.decode(buffer.getByteBuffer());
        messageBO.setStoreGroup(storeConfig.getGroup());
        buffer.release();

        return messageBO;
    }

    @Override
    public MessageBO select(long offset) {
        SelectedMappedBuffer buffer = selectBuffer(offset);
        if (buffer == null) {
            return MessageBO.notFound();
        }

        ByteBuffer byteBuffer = buffer.getByteBuffer();
        int size = byteBuffer.getInt();
        byteBuffer.limit(size);
        byteBuffer.rewind();

        MessageBO messageBO = MessageDecoder.decode(buffer.getByteBuffer());
        buffer.release();

        return messageBO;
    }

    @Override
    public SelectedMappedBuffer selectBuffer(long offset) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int) (offset % commitConfig.getFileSize());
        return mappedFile.select(position);
    }

    @Override
    public SelectedMappedBuffer selectBuffer(long offset, int size) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int) (offset % commitConfig.getFileSize());
        return mappedFile.select(position, size);
    }



    @Override
    public void destroy() {
        mappedFileQueue.destroy();
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
        MessageEncoder encoder = localEncoder.get().getEncoder();

        messageBO.setStoreHost(storeConfig.getHostAddress());
        messageBO.setStoreGroup(storeConfig.getGroup());

        long now = System.currentTimeMillis();
        messageBO.setStoreTimestamp(now);

        encoder.calculate(messageBO);
        return InsertContext.builder()
            .now(now)
            .messageBO(messageBO)
            .encoder(encoder)
            .build();
    }

    private MappedFile getMappedFile(int size) {
        MappedFile mappedFile = mappedFileQueue.getOrCreateMappedFileForSize(size);
        mappedFile.setFileMode(CLibrary.MADV_RANDOM);

        return mappedFile;
    }

    /**
     * @renamed from assignOffset to assignCommitOffset
     */
    private void assignCommitOffset(MessageBO messageBO, MappedFile mappedFile) {
        long commitLogOffset = mappedFile.getMinOffset() + mappedFile.getInsertPosition();
        messageBO.setCommitOffset(commitLogOffset);
    }

    private void handleInsertError(InsertResult insertResult) {
        switch (insertResult.getStatus()) {
            case PUT_OK -> {}
            case END_OF_FILE -> throw new EnqueueException(EnqueueStatus.END_OF_FILE);
            case MESSAGE_SIZE_EXCEEDED, PROPERTIES_SIZE_EXCEEDED -> throw new EnqueueException(EnqueueStatus.MESSAGE_ILLEGAL);
            default -> throw new EnqueueException(EnqueueStatus.UNKNOWN_ERROR);
        }
    }

    @Override
    public void start() throws Exception {
        flushPolicy.start();
    }

    @Override
    public void shutdown() throws Exception {
        flushPolicy.shutdown();
    }

    @Override
    public void initialize() throws Exception {
        load();
        recover();
    }

    private void load() {
        mappedFileQueue.load();
        mappedFileQueue.setFileMode(CLibrary.MADV_RANDOM);
        mappedFileQueue.checkSelf();
    }

    private void recover() {
        CommitLogRecovery commitLogRecovery = new CommitLogRecovery(this, checkPoint);
        commitLogRecovery.recover();
    }
}
