package com.wolfmq.store.domain.consumequeue.file;

import cn.coderule.common.util.lang.ThreadUtil;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.domain.consumequeue.QueueType;
import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.consumequeue.QueueUnit;
import com.wolfmq.common.core.mmap.SelectedMappedBuffer;
import com.wolfmq.common.domain.consumequeue.ConsumeQueue;
import com.wolfmq.common.core.mmap.MappedFile;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.store.infra.file.DefaultMappedFileQueue;
import com.wolfmq.common.domain.offset.QueueProgress;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @renamed from DefaultConsumeQueue to FileConsumeQueue
 */
@Slf4j
public class FileConsumeQueue implements ConsumeQueue {
    private static final AtomicLongFieldUpdater<FileConsumeQueue> MAX_OFFSET_UPDATER
        = AtomicLongFieldUpdater.newUpdater(FileConsumeQueue.class, "maxOffset");
    private static final AtomicLongFieldUpdater<FileConsumeQueue> MIN_OFFSET_UPDATER
        = AtomicLongFieldUpdater.newUpdater(FileConsumeQueue.class, "minOffset");

    @Getter
    private final String topic;
    @Getter
    private final int queueId;
    @Getter
    private MappedFileQueue mappedFileQueue;

    private final StoreConfig storeConfig;
    private final ConsumeQueueConfig queueConfig;
    private final QueueProgress queueProgress;
    private final ByteBuffer writeBuffer;

    private final ConcurrentMap<Integer, Long> commitOffsetMap;

    private volatile long minOffset = 0;
    private volatile long maxOffset = 0;

    public FileConsumeQueue(
        String topic,
        int queueId,
        StoreConfig storeConfig,
        QueueProgress queueProgress
    ) {
        this.topic = topic;
        this.queueId = queueId;
        this.storeConfig = storeConfig;
        this.queueConfig = storeConfig.getConsumeQueue();
        this.queueProgress = queueProgress;

        this.writeBuffer = ByteBuffer.allocate(this.queueConfig.getUnitSize());
        this.commitOffsetMap = new ConcurrentHashMap<>();

        initMappedFileQueue();
    }

    @Override
    public QueueType getQueueType() {
        return QueueType.FILE;
    }

    @Override
    public int getUnitSize() {
        return queueConfig.getUnitSize();
    }

    @Override
    public void enqueue(CommitEvent event) {
        if (!preEnqueue(event)) return;

        for (int i = 0; i < queueConfig.getMaxEnqueueRetry(); i++) {
            boolean success = insert(event);
            if (success) {
                postEnqueue(event);
                return;
            }

            log.warn("[BUG]ConsumeQueue.enqueue failed, retry {} times", i);
            ThreadUtil.sleep(1000);
        }

        log.error("[BUG]consume queue can not write, {} {}", this.topic, this.queueId);
    }

    @Override
    public QueueUnit get(long index) {
        if (index < MIN_OFFSET_UPDATER.get(this)) {
            return null;
        }

        long offset = index * queueConfig.getUnitSize();
        SelectedMappedBuffer buffer = select(offset, queueConfig.getUnitSize());
        if (buffer == null) {
            return null;
        }

        QueueUnit unit = createQueueUnit(offset, buffer);
        buffer.release();
        return unit;
    }

    @Override
    public List<QueueUnit> get(long index, int num) {
        long offset = index * queueConfig.getUnitSize();
        if (offset < MIN_OFFSET_UPDATER.get(this)) {
            return List.of();
        }

        SelectedMappedBuffer buffer = select(offset);
        if (buffer == null) {
            return List.of();
        }

        List<QueueUnit> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            if (!buffer.getByteBuffer().hasRemaining()) {
                break;
            }

            QueueUnit unit = createQueueUnit(offset, buffer);
            result.add(unit);
            offset += queueConfig.getUnitSize();
        }

        buffer.release();
        return result;
    }

    @Override
    public long getMinOffset() {
        return MIN_OFFSET_UPDATER.get(this);
    }

    @Override
    public void setMinOffset(long offset) {
        MIN_OFFSET_UPDATER.set(this, offset);
    }

    @Override
    public void setMaxOffset(long maxOffset) {
        MAX_OFFSET_UPDATER.set(this, maxOffset);
    }

    @Override
    public long rollToOffset(long offset) {
        int fileSize = queueConfig.getFileSize();
        int totalUnits = fileSize / queueConfig.getUnitSize();

        return offset + totalUnits - offset % totalUnits;
    }

    @Override
    public long getMaxOffset() {
        return MAX_OFFSET_UPDATER.get(this);
    }

    @Override
    public long increaseOffset() {
        return MAX_OFFSET_UPDATER.getAndAdd(this, 1);
    }

    @Override
    public void load() {
        mappedFileQueue.load();

        setMinOffset(mappedFileQueue.getMinOffset());
        setMaxOffset(mappedFileQueue.getMaxOffset());
    }

    public void flush() {
        flush(0);
    }

    public void flush(int minPages) {
        this.mappedFileQueue.flush(minPages);
    }

    @Override
    public void destroy() {
        mappedFileQueue.destroy();
    }

    private QueueUnit createQueueUnit(long offset, SelectedMappedBuffer buffer) {
        ByteBuffer byteBuffer = buffer.getByteBuffer();
        long commitOffset = byteBuffer.getLong();
        int messageSize = byteBuffer.getInt();
        long tagsCode = byteBuffer.getLong();

        return QueueUnit.builder()
            .queueOffset(offset / queueConfig.getUnitSize())
            .commitOffset(commitOffset)
            .messageSize(messageSize)
            .tagsCode(tagsCode)
            .unitSize(queueConfig.getUnitSize())
            .build();
    }

    private void initMappedFileQueue() {
        String path = storeConfig.getPath().getConsumeQueuePath()
            + File.separator
            + topic
            + File.separator
            + queueId;
        this.mappedFileQueue = new DefaultMappedFileQueue(path, queueConfig.getFileSize());
    }

    private SelectedMappedBuffer select(long offset) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int)(offset % queueConfig.getFileSize());
        return mappedFile.select(position);
    }

    private SelectedMappedBuffer select(long offset, int size) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int)(offset % queueConfig.getFileSize());
        return mappedFile.select(position, size);
    }

    private boolean insert(CommitEvent event) {
        MessageBO messageBO = event.getMessageBO();

        long offset = messageBO.getQueueOffset() * queueConfig.getUnitSize();
        if (!validateOffset(messageBO.getQueueOffset(), offset)) {
            return true;
        }

        MappedFile mappedFile = getMappedFile(messageBO.getQueueOffset(), offset);
        if (mappedFile == null) {
            return false;
        }

        setWriteBuffer(event);
        mappedFile.insert(this.writeBuffer);
        setMaxOffset(messageBO.getQueueOffset());
        return true;
    }

    /**
     * validate queue offset
     *  - if mappedFileQueue is empty, return true
     *  - if mappedFileQueue is not empty, the offset should be
     *      - in the range of the last MappedFile
     *      - or in the next last MappedFile
     *
     * @param queueIndex queueUnit index
     * @param queueOffset queue offset
     * @return true | false
     */
    private boolean validateOffset(long queueIndex, long queueOffset) {
        if (0 == queueIndex) {
            return true;
        }

        MappedFile last = mappedFileQueue.getLastMappedFile();
        if (last == null) {
            return true;
        }

        if (last.containsOffset(queueOffset)) {
            return !last.isFull();
        }

        if (queueOffset < last.getMinOffset()) {
            return false;
        }

        return queueOffset <= last.getMinOffset() + last.getFileSize() * 2L;
    }

    /**
     * get MappedFile by queue index and offset
     * if the MappedFile exists, return it
     * else create a new MappedFile and init it
     *
     * @param queueIndex queueUnit index
     * @param queueOffset queue offset
     * @return mappedFile
     */
    private MappedFile getMappedFile(long queueIndex, long queueOffset) {
        MappedFile last = mappedFileQueue.getLastMappedFile();
        if (last != null && last.containsOffset(queueOffset)) {
            return last;
        }

        long fileOffset = queueOffset - queueOffset % queueConfig.getFileSize();
        boolean isQueueEmpty = mappedFileQueue.isEmpty();
        MappedFile file = mappedFileQueue.createMappedFileByStartOffset(fileOffset);
        if (file == null) {
            return null;
        }

        if (!isQueueEmpty) {
            return file;
        }

        initMappedFile(file, queueIndex, queueOffset);
        return file;
    }

    private void initMappedFile(MappedFile mappedFile, long queueIndex, long queueOffset) {
        if (0 == queueIndex || 0 != mappedFile.getInsertPosition()) {
            return;
        }

//        if (0 == MIN_OFFSET_UPDATER.get(this) || queueOffset < MIN_OFFSET_UPDATER.get(this)) {
//            MIN_OFFSET_UPDATER.set(this, queueOffset);
//        }

        preFillBlank(mappedFile, queueOffset);
    }

    private void preFillBlank(final MappedFile mappedFile, final long size) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(queueConfig.getUnitSize());
        byteBuffer.putLong(0L);
        byteBuffer.putInt(Integer.MAX_VALUE);
        byteBuffer.putLong(0L);

        int until = (int) (size % mappedFile.getFileSize());
        for (int i = 0; i < until; i += queueConfig.getUnitSize()) {
            mappedFile.insert(byteBuffer.array());
        }
    }

    private void setWriteBuffer(CommitEvent event) {
        MessageBO messageBO = event.getMessageBO();
        this.writeBuffer.flip();
        this.writeBuffer.limit(queueConfig.getUnitSize());

        this.writeBuffer.putLong(messageBO.getCommitOffset());
        this.writeBuffer.putInt(messageBO.getMessageLength());
        this.writeBuffer.putLong(messageBO.getTagsCode());

        this.writeBuffer.flip();
        this.writeBuffer.limit(queueConfig.getUnitSize());
    }

    private boolean preEnqueue(CommitEvent event) {
        // check commitOffset, skip if

        // assign queue offset if needed

        return true;
    }

    /**
     * set maxCommitLogOffset and checkpoint info
     * @param event commitLogEvent
     */
    private void postEnqueue(CommitEvent event) {
        updateCommitOffset(event);
        saveOffsetToCheckpoint(event);
    }

    private void saveOffsetToCheckpoint(CommitEvent event) {
        long offset = event.getMessageBO().getQueueOffset();
        Long oldOffset = queueProgress.getQueueOffset(topic, queueId);

        if (oldOffset == null || oldOffset < offset) {
            queueProgress.updateQueueOffset(topic, queueId, offset);
        }
    }

    private void updateCommitOffset(CommitEvent event) {
        long offset = event.getMessageBO().getCommitOffset()
            + event.getMessageBO().getMessageLength();

        int shardId = event.getShardId();
        Long oldOffset = commitOffsetMap.get(shardId);
        if (oldOffset != null && oldOffset >  offset) {
            return;
        }

        commitOffsetMap.put(shardId, offset);
    }

}
