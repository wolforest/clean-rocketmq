package com.wolf.minimq.store.infra.file;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.wolf.common.util.io.BufferUtil;
import com.wolf.common.util.io.DirUtil;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.model.vo.AppendResult;
import com.wolf.minimq.domain.model.vo.SelectedMappedBuffer;
import com.wolf.minimq.store.infra.memory.CLibrary;
import com.wolf.minimq.store.infra.memory.TransientStorePool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class DefaultMappedFile extends ReferenceResource implements MappedFile {
    public static final int OS_PAGE_SIZE = 1024 * 4;

    protected String fileName;
    protected long offsetInFileName;
    protected int fileSize;

    protected File file;
    protected FileChannel fileChannel;
    protected MappedByteBuffer mappedByteBuffer;
    protected MappedByteBuffer mappedByteBufferWaitToClean;

    protected ByteBuffer writeCache;
    protected TransientStorePool transientStorePool;

    protected AtomicInteger writePosition = new AtomicInteger(0);
    protected AtomicInteger commitPosition = new AtomicInteger(0);
    protected AtomicInteger flushPosition = new AtomicInteger(0);

    protected volatile long storeTimestamp = 0;
    protected long startTimestamp = -1;
    protected long stopTimestamp = -1;

    public DefaultMappedFile() {}

    public DefaultMappedFile(String fileName, int fileSize, TransientStorePool transientStorePool) throws IOException {
        this(fileName, fileSize);

        this.writeCache = transientStorePool.borrowBuffer();
        this.transientStorePool = transientStorePool;
    }

    public DefaultMappedFile(String fileName, int fileSize) throws IOException {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file = new File(this.fileName);
        this.offsetInFileName = Long.parseLong(this.file.getName());

        DirUtil.createIfNotExists(this.file.getParent());
        this.initFile();
    }

    @Override
    public boolean isFull() {
        return this.fileSize == writePosition.get();
    }

    @Override
    public boolean hasEnoughSpace(int size) {
        return this.fileSize + size >= writePosition.get();
    }

    @Override
    public int getWritePosition() {
        return null == transientStorePool || !transientStorePool.isRealCommit()
            ? this.writePosition.get()
            : this.commitPosition.get();
    }

    @Override
    public AppendResult append(byte[] data) {
        return append(data, 0, data.length);
    }

    @Override
    public AppendResult append(ByteBuffer data) {
        int currentPosition = writePosition.get();
        if ((currentPosition + data.remaining()) > this.fileSize) {
            return AppendResult.endOfFile();
        }

        try {
            ByteBuffer buffer = this.mappedByteBuffer.slice();
            buffer.position(currentPosition);
            buffer.put(data);

            return AppendResult.success(currentPosition);
        } catch (Throwable e) {
            log.error("Error occurred when append message to mappedFile.", e);
            return AppendResult.failure();
        }
    }

    @Override
    public AppendResult append(byte[] data, int offset, int length) {
        int currentPosition = writePosition.get();
        if ((currentPosition + length) > this.fileSize) {
            return AppendResult.endOfFile();
        }

        try {
            ByteBuffer buffer = this.mappedByteBuffer.slice();
            buffer.position(currentPosition);
            buffer.put(data, offset, length);

            return AppendResult.success(currentPosition);
        } catch (Throwable e) {
            log.error("Error occurred when append message to mappedFile.", e);
            return AppendResult.failure();
        }
    }

    @Override
    public SelectedMappedBuffer select(int pos, int size) {
        int dataPosition = getWritePosition();
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
            .startOffset(this.offsetInFileName + pos)
            .byteBuffer(newBuffer)
            .size(size)
            .mappedFile(this)
            .build();
    }

    @Override
    public SelectedMappedBuffer select(int pos) {
        int dataPosition = getWritePosition();
        if (pos >= dataPosition || pos < 0) {
            return null;
        }

        int size = dataPosition - pos;
        return select(pos, size);
    }

    @Override
    public int flush(int flushLeastPages) {
        return 0;
    }

    @Override
    public MappedByteBuffer getMappedByteBuffer() {
        return null;
    }

    @Override
    public ByteBuffer sliceByteBuffer() {
        return null;
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
    public boolean destroy(long intervalForcibly) {
        this.shutdown(intervalForcibly);
        if (!this.isCleanupOver()) {
            log.warn("destroy mapped file[REF:" + this.getRefCount() + "] " + this.fileName
                + " Failed. cleanupOver: " + this.cleanupOver);
            return false;
        }

        try {
            this.fileChannel.close();
            log.info("close file channel {} OK", this.fileName);

            return this.file.delete();
        } catch (Exception e) {
            log.warn("close file channel {} Failed. ", this.fileName, e);
        }

        return true;
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
        int commit = commitPosition.get();
        int write = writePosition.get();

        if (this.isFull()) {
            return true;
        }

        if (commitLeastPages > 0) {
            return ((write / OS_PAGE_SIZE) - (commit / OS_PAGE_SIZE)) >= commitLeastPages;
        }

        return write > commit;
    }

    private boolean isAbleToFlush(final int flushLeastPages) {
        int flush = flushPosition.get();
        int write = getWritePosition();

        if (this.isFull()) {
            return true;
        }

        if (flushLeastPages > 0) {
            return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= flushLeastPages;
        }

        return write > flush;
    }

    private void mlock(Pointer pointer, long address, long beginTime) {
        int ret = CLibrary.INSTANCE.mlock(pointer, new NativeLong(this.fileSize));
        log.info("mlock {} {} {} ret = {} time consuming = {}", address, this.fileName, this.fileSize, ret, System.currentTimeMillis() - beginTime);
    }

    private void madvise(Pointer pointer, long address, long beginTime) {
        int ret = CLibrary.INSTANCE.madvise(pointer, new NativeLong(this.fileSize), CLibrary.MADV_WILLNEED);
        log.info("madvise {} {} {} ret = {} time consuming = {}", address, this.fileName, this.fileSize, ret, System.currentTimeMillis() - beginTime);
    }

}
