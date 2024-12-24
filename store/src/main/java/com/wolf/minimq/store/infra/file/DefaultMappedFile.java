package com.wolf.minimq.store.infra.file;

import com.wolf.common.util.io.BufferUtil;
import com.wolf.common.util.io.DirUtil;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.vo.AppendResult;
import com.wolf.minimq.domain.vo.SelectedMappedBuffer;
import com.wolf.minimq.store.infra.memory.TransientStorePool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
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
    public boolean append(byte[] data) {
        return false;
    }

    @Override
    public AppendResult append(ByteBuffer data) {
        return null;
    }

    @Override
    public AppendResult append(byte[] data, int offset, int length) {
        return null;
    }

    @Override
    public SelectedMappedBuffer select(int pos, int size) {
        return null;
    }

    @Override
    public SelectedMappedBuffer select(int pos) {
        return null;
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

}
