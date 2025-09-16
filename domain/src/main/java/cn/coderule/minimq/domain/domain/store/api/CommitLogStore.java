package cn.coderule.minimq.domain.domain.store.api;

import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;

/**
 * CommitLog APIs, for M/S
 */
public interface CommitLogStore {
    SelectedMappedBuffer select(long offset);
    InsertResult insert(long offset, byte[] data, int start, int size);

    long getMinOffset();
    long getMaxOffset();

    long getFlushedOffset();
    long getUnFlushedSize();
}
