package com.wolfmq.store.api;

import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.core.mmap.SelectedMappedBuffer;
import com.wolfmq.common.server.store.api.CommitLogStore;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;

public class CommitLogStoreImpl implements CommitLogStore {
    private final CommitLogFacade commitLogFacade;

    public CommitLogStoreImpl(CommitLogFacade commitLogFacade) {
        this.commitLogFacade = commitLogFacade;
    }
    @Override
    public SelectedMappedBuffer select(long offset) {
        return commitLogFacade.selectBuffer(offset);
    }

    @Override
    public InsertResult insert(long offset, byte[] data, int start, int size) {
        return commitLogFacade.insert(offset, data, start, size);
    }

    @Override
    public long getMinOffset(int shardId) {
        return commitLogFacade.getMinOffset(shardId);
    }

    @Override
    public long getMaxOffset(int shardId) {
        return commitLogFacade.getMaxOffset(shardId);
    }

    @Override
    public long getFlushedOffset(int shardId) {
        return commitLogFacade.getFlushedOffset(shardId);
    }

    @Override
    public long getUnFlushedSize(int shardId) {
        return commitLogFacade.getUnFlushedSize(shardId);
    }

}
