package com.wolf.minimq.store.infra.file;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.wolf.common.util.io.BufferUtil;
import com.wolf.common.util.io.DirUtil;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.model.dto.AppendResult;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import com.wolf.minimq.store.infra.memory.CLibrary;
import com.wolf.minimq.store.infra.memory.TransientPool;
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
import lombok.Getter;
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
    protected TransientPool transientPool;

    protected AtomicInteger writePosition = new AtomicInteger(0);
    protected AtomicInteger commitPosition = new AtomicInteger(0);
    protected AtomicInteger flushPosition = new AtomicInteger(0);

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
    public void setFileMode(int mode) {
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
    public int getWriteOrCommitPosition() {
        return null == transientPool || !transientPool.isRealCommit()
            ? this.writePosition.get()
            : this.commitPosition.get();
    }

    @Override
    public AppendResult insert(byte[] data) {
        return insert(data, 0, data.length);
    }

    @Override
    public AppendResult insert(ByteBuffer data) {
        int currentPosition = writePosition.get();
        if ((currentPosition + data.remaining()) > this.fileSize) {
            return AppendResult.endOfFile();
        }

        try {
            ByteBuffer buffer = this.mappedByteBuffer.slice();
            buffer.position(currentPosition);
            buffer.put(data);

            this.storeTimestamp = System.currentTimeMillis();
            return AppendResult.success(currentPosition);
        } catch (Throwable e) {
            log.error("Error occurred when append message to mappedFile.", e);
            return AppendResult.failure();
        }
    }

    @Override
    public AppendResult insert(byte[] data, int offset, int length) {
        int currentPosition = writePosition.get();
        if ((currentPosition + length) > this.fileSize) {
            return AppendResult.endOfFile();
        }

        try {
            ByteBuffer buffer = this.mappedByteBuffer.slice();
            buffer.position(currentPosition);
            buffer.put(data, offset, length);

            this.storeTimestamp = System.currentTimeMillis();
            return AppendResult.success(currentPosition);
        } catch (Throwable e) {
            log.error("Error occurred when append message to mappedFile.", e);
            return AppendResult.failure();
        }
    }

    @Override
    public SelectedMappedBuffer select(int pos, int size) {
        int dataPosition = getWriteOrCommitPosition();
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
        int dataPosition = getWriteOrCommitPosition();
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

        int readPosition = getWriteOrCommitPosition();
        if ((pos + size) > readPosition) {
            log.warn("selectMappedBuffer request pos invalid, request pos: {}, size: {}, fileFromOffset: {}", pos, size, this.offsetInFileName);
            return false;
        }

        if (!this.hold()) {
            log.debug("matched, but hold failed, request pos: {}, fileFromOffset: {}", pos, this.offsetInFileName);
            return false;
        }

        try {
            int readNum = fileChannel.read(byteBuffer, pos);
            return size == readNum;
        } catch (Throwable t) {
            log.warn("Get data failed pos:{} size:{} fileFromOffset:{}", pos, size, this.offsetInFileName);
            return false;
        } finally {
            this.release();
        }
    }

    @Override
    public int flush(int flushLeastPages) {
        if (!this.isAbleToFlush(flushLeastPages)) {
            return this.flushPosition.get();
        }

        if (!this.hold()) {
            return this.flushPosition.get();
        }

        int position = getWriteOrCommitPosition();
        try {
            if (null != writeCache && this.fileChannel.position() != 0) {
                this.fileChannel.force(false);
            } else {
                this.mappedByteBuffer.force();
            }
        } catch (Throwable e) {
            log.error("Error occurred when force data to disk.", e);
        }

        this.flushPosition.set(position);
        this.release();

        return this.flushPosition.get();
    }

    @Override
    public int commit(final int commitLeastPages) {
        if (writeCache == null) {
            //no need to commit data to file channel, so just regard wrotePosition as committedPosition.
            return writePosition.get();
        }

        //no need to commit data to file channel, so just set committedPosition to wrotePosition.
        if (transientPool != null && !transientPool.isRealCommit()) {
            commitPosition.set(writePosition.get());
        } else if (this.isAbleToCommit(commitLeastPages)) {
            if (this.hold()) {
                commit0();
                this.release();
            } else {
                log.warn("in commit, hold failed, commit offset = {}", commitPosition.get());
            }
        }

        // All dirty data has been committed to FileChannel.
        if (writeCache != null && this.transientPool != null && this.fileSize == commitPosition.get()) {
            this.transientPool.returnBuffer(writeCache);
            this.writeCache = null;
        }

        return commitPosition.get();
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
        int write = getWriteOrCommitPosition();

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

    protected void commit0() {
        int writePos = writePosition.get();
        int lastCommittedPosition = commitPosition.get();

        if (writePos - lastCommittedPosition <= 0) {
            return;
        }

        try {
            ByteBuffer byteBuffer = writeCache.slice();
            byteBuffer.position(lastCommittedPosition);
            byteBuffer.limit(writePos);
            this.fileChannel.position(lastCommittedPosition);
            this.fileChannel.write(byteBuffer);
            commitPosition.set(writePos);
        } catch (Throwable e) {
            log.error("Error occurred when commit data to FileChannel.", e);
        }
    }

    @Override
    public int getWritePosition() {
        return writePosition.get();
    }

    @Override
    public void setWritePosition(int writePosition) {
        this.writePosition.set(writePosition);
    }

    @Override
    public int getCommitPosition() {
        return commitPosition.get();
    }

    @Override
    public void setCommitPosition(int commitPosition) {
        this.commitPosition.set(commitPosition);
    }

    @Override
    public int getFlushPosition() {
        return this.flushPosition.get();
    }

    @Override
    public void setFlushPosition(int flushPosition) {
        this.flushPosition.set(flushPosition);
    }

}
