package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.common.util.lang.ThreadUtil;
import com.wolf.minimq.domain.config.ConsumeQueueConfig;
import com.wolf.minimq.domain.enums.QueueType;
import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.store.infra.file.DefaultMappedFileQueue;
import com.wolf.minimq.store.server.StoreCheckpoint;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultConsumeQueue implements ConsumeQueue {
    private static final AtomicLongFieldUpdater<DefaultConsumeQueue> MAX_OFFSET_UPDATER = AtomicLongFieldUpdater.newUpdater(DefaultConsumeQueue.class, "maxOffset");
    private static final AtomicLongFieldUpdater<DefaultConsumeQueue> MIN_OFFSET_UPDATER = AtomicLongFieldUpdater.newUpdater(DefaultConsumeQueue.class, "minOffset");

    @Getter
    private final String topic;
    @Getter
    private final int queueId;
    @Getter
    private MappedFileQueue mappedFileQueue;

    private final ConsumeQueueConfig config;
    private final StoreCheckpoint checkpoint;
    private final ByteBuffer writeBuffer;

    @Getter @Setter
    private long maxCommitLogOffset = 0L;

    private volatile long minOffset = 0;
    private volatile long maxOffset = 0;

    public DefaultConsumeQueue(String topic, int queueId, ConsumeQueueConfig config, StoreCheckpoint checkpoint) {
        this.topic = topic;
        this.queueId = queueId;
        this.config = config;
        this.checkpoint = checkpoint;

        this.writeBuffer = ByteBuffer.allocate(config.getUnitSize());
        initMappedFileQueue();
    }

    @Override
    public QueueType getQueueType() {
        return QueueType.DEFAULT;
    }

    @Override
    public void enqueue(CommitLogEvent event) {
        for (int i = 0; i < config.getMaxEnqueueRetry(); i++) {
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
    public QueueUnit fetch(long index) {
        long offset = index * config.getUnitSize();
        if (offset <= MIN_OFFSET_UPDATER.get(this)) {
            return null;
        }

        SelectedMappedBuffer buffer = select(offset);
        if (buffer == null) {
            return null;
        }

        QueueUnit unit = createQueueUnit(offset, buffer);
        buffer.release();
        return unit;
    }

    @Override
    public List<QueueUnit> fetch(long index, int num) {
        long offset = index * config.getUnitSize();
        if (offset <= MIN_OFFSET_UPDATER.get(this)) {
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
            offset += config.getUnitSize();
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
    public long getMaxOffset() {
        return MAX_OFFSET_UPDATER.get(this);
    }

    @Override
    public long increaseOffset() {
        return MAX_OFFSET_UPDATER.addAndGet(this, 1);
    }

    private QueueUnit createQueueUnit(long offset, SelectedMappedBuffer buffer) {
        return QueueUnit.builder()
            .queueOffset(offset)
            .commitLogOffset(buffer.getByteBuffer().getLong())
            .messageSize(buffer.getByteBuffer().getInt())
            .tagsCode(buffer.getByteBuffer().getLong())
            .build();
    }

    private void initMappedFileQueue() {
        String path = config.getRootPath()
            + File.separator
            + topic
            + File.separator
            + queueId;
        this.mappedFileQueue = new DefaultMappedFileQueue(path, config.getFileSize());
    }

    private SelectedMappedBuffer select(long offset) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (mappedFile == null) {
            return null;
        }

        int position = (int)(offset % config.getFileSize());
        return mappedFile.select(position);
    }

    private boolean insert(CommitLogEvent event) {
        MessageBO messageBO = event.getMessageBO();

        long offset = messageBO.getQueueOffset() * config.getUnitSize();
        if (!validateOffset(messageBO.getQueueOffset(), offset)) {
            return true;
        }

        MappedFile mappedFile = getMappedFile(messageBO.getQueueOffset(), offset);
        if (mappedFile == null) {
            return false;
        }

        setWriteBuffer(event);
        mappedFile.insert(writeBuffer);
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

        if (queueOffset < last.getOffsetInFileName()) {
            return false;
        }

        return queueOffset <= last.getOffsetInFileName() + last.getFileSize() * 2L;
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

        long fileOffset = queueOffset - queueOffset % config.getFileSize();
        MappedFile file = mappedFileQueue.createMappedFile(fileOffset);
        if (file == null) {
            return null;
        }

        initMappedFile(file, queueIndex, queueOffset);
        return file;
    }

    private void initMappedFile(MappedFile mappedFile, long queueIndex, long queueOffset) {
        if (0 == queueIndex || 0 != mappedFile.getWritePosition()) {
            return;
        }

//        if (0 == MIN_OFFSET_UPDATER.get(this) || queueOffset < MIN_OFFSET_UPDATER.get(this)) {
//            MIN_OFFSET_UPDATER.set(this, queueOffset);
//        }

        preFillBlank(mappedFile, queueOffset);
    }

    private void preFillBlank(final MappedFile mappedFile, final long size) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(config.getUnitSize());
        byteBuffer.putLong(0L);
        byteBuffer.putInt(Integer.MAX_VALUE);
        byteBuffer.putLong(0L);

        int until = (int) (size % mappedFile.getFileSize());
        for (int i = 0; i < until; i += config.getUnitSize()) {
            mappedFile.insert(byteBuffer.array());
        }
    }

    private void setWriteBuffer(CommitLogEvent event) {
        MessageBO messageBO = event.getMessageBO();
        this.writeBuffer.flip();
        this.writeBuffer.limit(config.getUnitSize());
        this.writeBuffer.putLong(messageBO.getCommitLogOffset());
        this.writeBuffer.putInt(messageBO.getStoreSize());
        this.writeBuffer.putLong(messageBO.getTagsCode());
    }

    /**
     * set maxCommitLogOffset and checkpoint info
     * @param event commitLogEvent
     */
    private void postEnqueue(CommitLogEvent event) {
        long offset = event.getMessageBO().getCommitLogOffset() + event.getMessageBO().getStoreSize();
        if (offset > maxCommitLogOffset) {
            maxCommitLogOffset = offset;
        }

        checkpoint.setConsumeQueueFlushTime(event.getMessageBO().getStoreTimestamp());
    }

}
