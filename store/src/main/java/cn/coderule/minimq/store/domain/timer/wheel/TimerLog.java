package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.domain.cluster.store.SelectedMappedBuffer;
import cn.coderule.minimq.domain.domain.timer.wheel.Block;
import cn.coderule.minimq.domain.domain.cluster.store.infra.MappedFile;
import cn.coderule.minimq.domain.domain.cluster.store.infra.MappedFileQueue;
import cn.coderule.minimq.store.infra.file.DefaultMappedFileQueue;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;

/**
 * TimerLog stores TimerMessage's delayInfo and offsetInfo
 * TimerLog has 3 operations:
 *   - append timer message
 *   - fetch timer message by offset
 *   - calculate offset for cleaning expired files
 */
@Slf4j
public class TimerLog {
    public final static int BLANK_MAGIC_CODE = 0xBBCCDDEE ^ 1880681586 + 8;
    private final static int MIN_BLANK_LEN = 4 + 8 + 4;
    public final static int UNIT_SIZE = Block.SIZE;

    /**
     * size for:
     *  unitSize + prePos + magic
     *  + currWriteTime + delayedTime
     */
    public final static int UNIT_PRE_SIZE_FOR_MSG = 28;
    public final static int UNIT_PRE_SIZE_FOR_METRIC = 40;
    private final MappedFileQueue mappedFileQueue;

    private final int fileSize;

    public TimerLog(final String storePath, final int fileSize) {
        this.fileSize = fileSize;
        this.mappedFileQueue = new DefaultMappedFileQueue(storePath, fileSize, null);
    }

    public boolean load() {
        return this.mappedFileQueue.load();
    }

    /**
     * append by block unit object
     * the only one public append API
     *
     * called by TimerWheelPersistence,
     *      and pos is always 0;
     *      and len is always Block.SIZE;
     *
     * @param block block object
     * @param pos   position or offset
     * @param len   len of block,current is fixed length Block.SIZE
     * @return offset
     */
    public long append(Block block, int pos, int len) {
        return append(block.bytes(), pos, len);
    }

    /**
     * just for test, no public usage.
     *
     * @param data data
     * @return offset
     */
    public long append(byte[] data) {
        return append(data, 0, data.length);
    }

    private long append(byte[] data, int pos, int len) {
        MappedFile mappedFile = chooseLastMappedFile(len);
        assert mappedFile != null;

        long currPosition = mappedFile.getMinOffset() + mappedFile.getWritePosition();
        if (!mappedFile.insert(data, pos, len).isSuccess()) {
            log.error("Append error for timer log");
            return -1;
        }
        return currPosition;
    }

    /**
     * fetch TimerMessage by offset
     *
     * @param offset offset
     * @return TimerMessage related SelectedMappedBuffer
     */
    public SelectedMappedBuffer getTimerMessage(long offset) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offset);
        if (null == mappedFile)
            return null;
        return mappedFile.select((int) (offset % mappedFile.getFileSize()));
    }

    /**
     * fetch the whole TimerRequest result
     *
     * @param offsetPy offset
     * @return whole timer message in the related MappedFile
     */
    public SelectedMappedBuffer getWholeBuffer(long offsetPy) {
        MappedFile mappedFile = mappedFileQueue.getMappedFileByOffset(offsetPy);
        if (null == mappedFile)
            return null;
        return mappedFile.select(0);
    }

    public void shutdown() {
        this.mappedFileQueue.flush(0);
        //it seems not need to call shutdown
    }

    /**
     * calculate offset for last unit
     * be careful.
     * if the format of timerLog changed, this offset has to be changed too
     * so dose the batch writing
     * @return offset
     */
    public int getOffsetForLastUnit() {
        return fileSize - (fileSize - MIN_BLANK_LEN) % UNIT_SIZE - MIN_BLANK_LEN - UNIT_SIZE;
    }

    private MappedFile chooseLastMappedFile(int len) {
        MappedFile mappedFile = this.mappedFileQueue.getLastMappedFile();
        if (mappedFile == null) {
            mappedFile = this.mappedFileQueue.createMappedFile(0L);
        } else if (mappedFile.isFull()) {
            mappedFile = this.mappedFileQueue.createMappedFile(mappedFile.getMaxOffset());
        }

        if (null == mappedFile) {
            log.error("Create mapped file1 error for timer log");
            return null;
        }

        if (len + MIN_BLANK_LEN <= mappedFile.getFileSize() - mappedFile.getWritePosition()) {
            return mappedFile;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(MIN_BLANK_LEN);
        byteBuffer.putInt(mappedFile.getFileSize() - mappedFile.getWritePosition());
        byteBuffer.putLong(0);
        byteBuffer.putInt(BLANK_MAGIC_CODE);

        if (mappedFile.insert(byteBuffer.array()).isSuccess()) {
            //need to set the wrote position
            mappedFile.setWritePosition(mappedFile.getFileSize());
        } else {
            log.error("Append blank error for timer log");
            return null;
        }

        mappedFile = this.mappedFileQueue.getLastMappedFile();
        if (mappedFile == null) {
            mappedFile = this.mappedFileQueue.createMappedFile(0L);
        }

        if (null == mappedFile) {
            log.error("create mapped file2 error for timer log");
            return null;
        }

        return mappedFile;
    }


}
