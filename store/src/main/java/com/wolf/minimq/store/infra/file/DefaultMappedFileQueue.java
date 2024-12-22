package com.wolf.minimq.store.infra.file;

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
}
