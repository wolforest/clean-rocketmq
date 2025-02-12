package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.store.infra.memory.TransientPool;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import cn.coderule.common.util.io.BufferUtil;
import cn.coderule.common.util.io.DirUtil;
import cn.coderule.minimq.domain.enums.FlushType;
import cn.coderule.minimq.domain.service.store.infra.MappedFile;
import cn.coderule.minimq.domain.model.dto.InsertResult;
import cn.coderule.minimq.domain.model.dto.SelectedMappedBuffer;
import cn.coderule.minimq.store.infra.memory.CLibrary;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMappedFile extends ReferenceResource implements MappedFile {
    public static final int OS_PAGE_SIZE = 1024 * 4;

    protected static final AtomicIntegerFieldUpdater<DefaultMappedFile> WRITE_POSITION_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(DefaultMappedFile.class, "writePosition");
    protected static final AtomicIntegerFieldUpdater<DefaultMappedFile> COMMIT_POSITION_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(DefaultMappedFile.class, "commitPosition");
    protected static final AtomicIntegerFieldUpdater<DefaultMappedFile> FLUSH_POSITION_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(DefaultMappedFile.class, "flushPosition");

    @Getter
    protected String fileName;
    @Getter
    protected long minOffset;
    @Getter
    protected long maxOffset;
    @Getter
    protected int fileSize;

    protected File file;
    protected FileChannel fileChannel;
    protected MappedByteBuffer mappedByteBuffer;
    protected MappedByteBuffer mappedByteBufferWaitToClean;

    protected ByteBuffer writeCache;
    protected TransientPool transientPool;

    protected volatile int writePosition  = 0;
    protected volatile int commitPosition = 0;
    protected volatile int flushPosition  = 0;

    @Getter
    protected volatile long storeTimestamp = 0;

    public DefaultMappedFile() {}

    public DefaultMappedFile(String fileName, int fileSize, TransientPool transientPool) throws IOException {
        this(fileName, fileSize);

        this.writeCache = transientPool.borrowBuffer();
        this.transientPool = transientPool;
    }

    public DefaultMappedFile(String fileName, int fileSize) throws IOException {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file = new File(this.fileName);
        this.minOffset = Long.parseLong(this.file.getName());
        this.maxOffset = this.minOffset + this.fileSize;

        DirUtil.createIfNotExists(this.file.getParent());
        this.initFile();
    }

    @Override
    public boolean isFull() {
        return this.fileSize == WRITE_POSITION_UPDATER.get(this);
    }

    @Override
    public boolean hasSpace(int size) {
        return WRITE_POSITION_UPDATER.get(this) + size < this.fileSize;
    }

    @Override
    public boolean containsOffset(long offset) {
        return offset >= this.minOffset && offset < this.maxOffset;
    }

    @Override
    public void setFileMode(int mode) {
        // add not windows check
        long address = BufferUtil.directBufferAddress(mappedByteBuffer);
        int madvise = CLibrary.INSTANCE.madvise(
            new Pointer(address), new NativeLong(fileSize), mode
        );

        if (madvise != 0) {
            log.error("setFileMode error fileName: {}, madvise: {}, mode:{}",
                fileName, madvise, mode);
        }
    }

    @Override
    public int getInsertPosition() {
        return null == transientPool || !transientPool.isRealCommit()
            ? WRITE_POSITION_UPDATER.get(this)
            : COMMIT_POSITION_UPDATER.get(this);
    }

    @Override
    public void setInsertOffset(long insertOffset) {
        int position = (int) insertOffset % this.fileSize;
        setInsertPosition(position);
    }

    @Override
    public void setInsertPosition(int insertPosition) {
        WRITE_POSITION_UPDATER.set(this, insertPosition);
        COMMIT_POSITION_UPDATER.set(this, insertPosition);
        FLUSH_POSITION_UPDATER.set(this, insertPosition);
    }

    @Override
    public InsertResult insert(byte[] data) {
        return insert(data, 0, data.length);
    }

    @Override
    public InsertResult insert(ByteBuffer data) {
        int currentPosition = WRITE_POSITION_UPDATER.get(this);
        if ((currentPosition + data.remaining()) > this.fileSize) {
            return InsertResult.endOfFile();
        }

        try {
            ByteBuffer buffer = this.mappedByteBuffer.slice();
            buffer.position(currentPosition);
            buffer.put(data);

            this.storeTimestamp = System.currentTimeMillis();
            return InsertResult.success(currentPosition);
        } catch (Throwable e) {
            log.error("Error occurred when append message to mappedFile.", e);
            return InsertResult.failure();
        }
    }

    @Override
    public InsertResult insert(byte[] data, int offset, int length) {
        int currentPosition = WRITE_POSITION_UPDATER.get(this);
        if ((currentPosition + length) > this.fileSize) {
            return InsertResult.endOfFile();
        }

        try {
            ByteBuffer buffer = this.mappedByteBuffer.slice();
            buffer.position(currentPosition);
            buffer.put(data, offset, length);

            this.storeTimestamp = System.currentTimeMillis();
            return InsertResult.success(currentPosition);
        } catch (Throwable e) {
            log.error("Error occurred when append message to mappedFile.", e);
            return InsertResult.failure();
        }
    }

    @Override
    public SelectedMappedBuffer select(int pos, int size) {
        int dataPosition = getInsertPosition();
        if ((pos + size) > dataPosition) {
            return null;
        }

        if (!this.hold()) {
            return null;
        }

        ByteBuffer buffer = this.mappedByteBuffer.slice();
        buffer.position(pos);

        ByteBuffer newBuffer = buffer.slice();
        newBuffer.limit(size);

        return SelectedMappedBuffer.builder()
            .startOffset(this.minOffset + pos)
            .byteBuffer(newBuffer)
            .size(size)
            .mappedFile(this)
            .build();
    }

    @Override
    public SelectedMappedBuffer select(int pos) {
        int dataPosition = getInsertPosition();
        if (pos >= dataPosition || pos < 0) {
            return null;
        }

        int size = dataPosition - pos;
        return select(pos, size);
    }

    @Override
    public boolean selectFromChannel(int pos, int size, ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() < size) {
            return false;
        }

        int readPosition = getInsertPosition();
        if ((pos + size) > readPosition) {
            log.warn("selectMappedBuffer request pos invalid, request pos: {}, size: {}, fileFromOffset: {}", pos, size, this.minOffset);
            return false;
        }

        if (!this.hold()) {
            log.debug("matched, but hold failed, request pos: {}, fileFromOffset: {}", pos, this.minOffset);
            return false;
        }

        try {
            int readNum = fileChannel.read(byteBuffer, pos);
            return size == readNum;
        } catch (Throwable t) {
            log.warn("Get data failed pos:{} size:{} fileFromOffset:{}", pos, size, this.minOffset);
            return false;
        } finally {
            this.release();
        }
    }

    @Override
    public int flush(int minPages) {
        if (!this.isAbleToFlush(minPages)) {
            return FLUSH_POSITION_UPDATER.get(this);
        }

        if (!this.hold()) {
            return FLUSH_POSITION_UPDATER.get(this);
        }

        int position = getInsertPosition();
        try {
            if (null != writeCache && this.fileChannel.position() != 0) {
                this.fileChannel.force(false);
            } else {
                this.mappedByteBuffer.force();
            }
        } catch (Throwable e) {
            log.error("Error occurred when force data to disk.", e);
        }

        FLUSH_POSITION_UPDATER.set(this, position);
        this.release();

        return FLUSH_POSITION_UPDATER.get(this);
    }

    @Override
    public int commit(final int minPages) {
        if (writeCache == null) {
            //no need to commit data to file channel, so just regard wrotePosition as committedPosition.
            return WRITE_POSITION_UPDATER.get(this);
        }

        //no need to commit data to file channel, so just set committedPosition to wrotePosition.
        if (transientPool != null && !transientPool.isRealCommit()) {
            COMMIT_POSITION_UPDATER.set(this, WRITE_POSITION_UPDATER.get(this));
        } else if (this.isAbleToCommit(minPages)) {
            if (this.hold()) {
                commit0();
                this.release();
            } else {
                log.warn("hold failed while commit, commit offset = {}", COMMIT_POSITION_UPDATER.get(this));
            }
        }

        // All dirty data has been committed to FileChannel.
        if (writeCache != null && this.transientPool != null && this.fileSize == COMMIT_POSITION_UPDATER.get(this)) {
            this.transientPool.returnBuffer(writeCache);
            this.writeCache = null;
        }

        return COMMIT_POSITION_UPDATER.get(this);
    }

    @Override
    public MappedByteBuffer getMappedByteBuffer() {
        return mappedByteBuffer;
    }

    @Override
    public ByteBuffer sliceByteBuffer() {
        return mappedByteBuffer.slice();
    }

    @Override
    public void warmup(int size) {
        warmup(FlushType.ASYNC, size);
    }

    @Override
    public void warmup(FlushType flushType, int size) {
        ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
        for (int i = 0; i < size; i++) {
            byteBuffer.put((byte)0);
        }

        if (flushType == FlushType.SYNC) {
            this.mappedByteBuffer.force();
        }
    }

    @Override
    public boolean cleanup(long currentRef) {
        if (this.isAvailable()) {
            return false;
        }

        if (this.isCleanupOver()) {
            return true;
        }

        BufferUtil.cleanBuffer(this.mappedByteBuffer);
        BufferUtil.cleanBuffer(this.mappedByteBufferWaitToClean);
        this.mappedByteBufferWaitToClean = null;

        return true;
    }

    @Override
    public void destroy(long interval) {
        this.shutdown(interval);
        if (!this.isCleanupOver()) {
            log.warn("destroy mapped file[REF:{}] {} Failed. cleanupOver: {}", this.getRefCount(), this.fileName, this.cleanupOver);
            return;
        }

        try {
            this.fileChannel.close();
            log.info("close file channel {} OK", this.fileName);

            this.file.delete();
        } catch (Exception e) {
            log.warn("close file channel {} Failed. ", this.fileName, e);
        }

    }

    private void initFile() throws IOException {
        boolean ok = false;

        try {
            this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
            this.mappedByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            ok = true;
        } catch (FileNotFoundException e) {
            log.error("Failed to create file {}", this.fileName, e);
            throw e;
        } catch (IOException e) {
            log.error("Failed to map file {}", this.fileName, e);
            throw e;
        } finally {
            if (!ok && this.fileChannel != null) {
                this.fileChannel.close();
            }
        }
    }

    private boolean isAbleToCommit(final int commitLeastPages) {
        int commit = COMMIT_POSITION_UPDATER.get(this);
        int write = WRITE_POSITION_UPDATER.get(this);

        if (this.isFull()) {
            return true;
        }

        if (commitLeastPages > 0) {
            return ((write / OS_PAGE_SIZE) - (commit / OS_PAGE_SIZE)) >= commitLeastPages;
        }

        return write > commit;
    }

    private boolean isAbleToFlush(final int minPages) {
        int flushPosition = FLUSH_POSITION_UPDATER.get(this);
        int writePosition = getInsertPosition();

        if (this.isFull()) {
            return true;
        }

        if (minPages > 0) {
            return (writePosition - flushPosition) / OS_PAGE_SIZE >= minPages;
        }

        return writePosition > flushPosition;
    }

    private void mlock(Pointer pointer, long address, long beginTime) {
        int ret = CLibrary.INSTANCE.mlock(pointer, new NativeLong(this.fileSize));
        log.info("mlock {} {} {} ret = {} time consuming = {}", address, this.fileName, this.fileSize, ret, System.currentTimeMillis() - beginTime);
    }

    private void madvise(Pointer pointer, long address, long beginTime) {
        int ret = CLibrary.INSTANCE.madvise(pointer, new NativeLong(this.fileSize), CLibrary.MADV_WILLNEED);
        log.info("madvise {} {} {} ret = {} time consuming = {}", address, this.fileName, this.fileSize, ret, System.currentTimeMillis() - beginTime);
    }

    protected void commit0() {
        int writePos = WRITE_POSITION_UPDATER.get(this);
        int lastCommittedPosition = COMMIT_POSITION_UPDATER.get(this);

        if (writePos - lastCommittedPosition <= 0) {
            return;
        }

        try {
            ByteBuffer byteBuffer = writeCache.slice();
            byteBuffer.position(lastCommittedPosition);
            byteBuffer.limit(writePos);
            this.fileChannel.position(lastCommittedPosition);
            this.fileChannel.write(byteBuffer);
            COMMIT_POSITION_UPDATER.set(this, writePos);
        } catch (Throwable e) {
            log.error("Error occurred when commit data to FileChannel.", e);
        }
    }

    @Override
    public int getWritePosition() {
        return WRITE_POSITION_UPDATER.get(this);
    }

    @Override
    public int getCommitPosition() {
        return COMMIT_POSITION_UPDATER.get(this);
    }

    @Override
    public int getFlushPosition() {
        return FLUSH_POSITION_UPDATER.get(this);
    }

}
