package com.wolf.minimq.store.infra.file;

import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;

public class DefaultMappedFileQueue implements MappedFileQueue {
    private final String dir;
    private final int fileSize;
    private final AllocateMappedFileService allocateMappedFileService;

    public DefaultMappedFileQueue(String dir, int fileSize, AllocateMappedFileService allocateMappedFileService) {
        this.dir = dir;
        this.fileSize = fileSize;
        this.allocateMappedFileService = allocateMappedFileService;
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public void checkSelf() {

    }

    @Override
    public MappedFile getAvailableMappedFile(int messageSize) {
        return null;
    }

    @Override
    public MappedFile getMappedFileByOffset(long offset) {
        return null;
    }

    @Override
    public MappedFile createMappedFileForOffset(long offset) {
        return null;
    }
}
