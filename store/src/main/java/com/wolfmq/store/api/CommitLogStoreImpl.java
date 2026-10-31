package com.wolfmq.store.api;

import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.domain.core.mmap.SelectedMappedBuffer;
import com.wolfmq.domain.server.store.api.CommitLogStore;
import com.wolfmq.store.domain.commitlog.CommitLogProxy;

public class CommitLogStoreImpl implements CommitLogStore {
    private final CommitLogProxy commitLogProxy;

    public CommitLogStoreImpl(CommitLogProxy commitLogProxy) {
        this.commitLogProxy = commitLogProxy;
    }
    @Override
    public SelectedMappedBuffer select(long offset) {
        return commitLogProxy.selectBuffer(offset);
    }

    @Override
    public InsertResult insert(long offset, byte[] data, int start, int size) {
        return commitLogProxy.insert(offset, data, start, size);
    }

    @Override
    public long getMinOffset(int shardId) {
        return commitLogProxy.getMinOffset(shardId);
    }

    @Override
    public long getMaxOffset(int shardId) {
        return commitLogProxy.getMaxOffset(shardId);
    }

    @Override
    public long getFlushedOffset(int shardId) {
        return commitLogProxy.getFlushedOffset(shardId);
    }

    @Override
    public long getUnFlushedSize(int shardId) {
        return commitLogProxy.getUnFlushedSize(shardId);
    }

}
