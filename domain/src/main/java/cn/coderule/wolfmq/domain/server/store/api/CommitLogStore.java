package cn.coderule.wolfmq.domain.server.store.api;

import cn.coderule.wolfmq.domain.core.mmap.InsertResult;
import cn.coderule.wolfmq.domain.core.mmap.SelectedMappedBuffer;

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
