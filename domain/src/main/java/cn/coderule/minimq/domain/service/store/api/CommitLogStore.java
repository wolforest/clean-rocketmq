package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.cluster.store.SelectedMappedBuffer;

/**
 * CommitLog APIs, for M/S
 */
public interface CommitLogStore {
    SelectedMappedBuffer select(long offset);
    InsertResult insert(long offset, byte[] data, int start, int size);
}
