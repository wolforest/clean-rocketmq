package com.wolfmq.domain.server.store.api;

import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.domain.core.mmap.SelectedMappedBuffer;

/**
 * CommitLog APIs, for M/S
 */
public interface CommitLogStore {
    SelectedMappedBuffer select(long offset);
    InsertResult insert(long offset, byte[] data, int start, int size);

    long getMinOffset(int shardId);
    long getMaxOffset(int shardId);

    long getFlushedOffset(int shardId);
    long getUnFlushedSize(int shardId);
}
