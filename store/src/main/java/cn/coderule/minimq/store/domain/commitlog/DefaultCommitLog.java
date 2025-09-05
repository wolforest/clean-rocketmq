package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogFlusher;
import cn.coderule.minimq.store.domain.commitlog.vo.InsertContext;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.core.exception.EnqueueException;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.cluster.store.SelectedMappedBuffer;
import cn.coderule.minimq.domain.service.store.infra.MappedFile;
import cn.coderule.minimq.domain.domain.message.MessageDecoder;
import cn.coderule.minimq.domain.core.lock.commitlog.CommitLogLock;
import cn.coderule.minimq.domain.core.lock.commitlog.CommitLogReentrantLock;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.store.domain.commitlog.vo.EnqueueThreadLocal;
import cn.coderule.minimq.store.infra.memory.CLibrary;
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
    private final StoreConfig storeConfig;
    private final CommitConfig commitConfig;
    private final MessageConfig messageConfig;
    private final CommitLogFlusher commitLogFlusher;

    @Getter
    private final MappedFileQueue mappedFileQueue;
    private final CommitLogLock commitLogLock;
    private ThreadLocal<EnqueueThreadLocal> localEncoder;

    public DefaultCommitLog(
        StoreConfig storeConfig,
        MappedFileQueue mappedFileQueue,
        CommitLogFlusher commitLogFlusher
    ) {
        this.storeConfig = storeConfig;
        this.commitConfig = storeConfig.getCommitConfig();
        this.messageConfig = storeConfig.getMessageConfig();

        this.mappedFileQueue = mappedFileQueue;
        this.commitLogFlusher = commitLogFlusher;

        this.commitLogLock = new CommitLogReentrantLock();
        initLocalEncoder();
    }

    @Override
    public InsertFuture insert(MessageBO messageBO) {
        InsertContext context = initContext(messageBO);

        this.commitLogLock.lock();
        try {

            MappedFile mappedFile = getMappedFile(messageBO.getMessageLength());
            assignCommitOffset(messageBO, mappedFile);

            ByteBuffer messageBuffer = context.getEncoder().encode(messageBO);
            InsertResult insertResult = mappedFile.insert(messageBuffer);
            handleInsertError(insertResult);

            return commitLogFlusher.flush(insertResult, messageBO);
        } catch (EnqueueException messageException) {
            return InsertFuture.failure(messageException.getStatus());
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
        buffer.release();

        return messageBO;
    }

    @Override
    public MessageBO select(long offset) {
        SelectedMappedBuffer buffer = selectBuffer(offset);
        if (buffer == null) {
            return MessageBO.notFound();
        }

        int size = buffer.getByteBuffer().getInt();
        buffer.getByteBuffer().limit(size);

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
    public long getMinOffset() {
        return mappedFileQueue.getMinOffset();
    }

    @Override
    public long getMaxOffset() {
        return mappedFileQueue.getMaxOffset();
    }

    @Override
    public long getCommitOffset(int size) {
        MappedFile mappedFile = getMappedFile(size);
        return mappedFile.getMinOffset() + mappedFile.getWritePosition();
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
        messageBO.setStoreHost(storeConfig.getHostAddress());
        messageBO.setBrokerName(storeConfig.getGroup());

        long now = System.currentTimeMillis();
        messageBO.setStoreTimestamp(now);

        return InsertContext.builder()
            .now(now)
            .messageBO(messageBO)
            .encoder(localEncoder.get().getEncoder())
            .build();
    }

    private MappedFile getMappedFile(int size) {
        MappedFile mappedFile = mappedFileQueue.createMappedFileForSize(size);
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

}
