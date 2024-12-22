package com.wolf.minimq.store.infra.file;

import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.vo.AppendResult;
import com.wolf.minimq.domain.vo.SelectedMappedBuffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public class DefaultMappedFile implements MappedFile {
    @Override
    public String getFileName() {
        return "";
    }

    @Override
    public long getOffsetInFileName() {
        return 0;
    }

    @Override
    public int getFileSize() {
        return 0;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isAvailable() {
        return false;
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
}
