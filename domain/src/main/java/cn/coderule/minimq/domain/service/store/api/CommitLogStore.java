package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.dto.InsertResult;
import cn.coderule.minimq.domain.dto.SelectedMappedBuffer;

/**
 * CommitLog APIs, for M/S
 */
public interface CommitLogStore {
    SelectedMappedBuffer select(long offset);
    InsertResult insert(long offset, byte[] data, int start, int size);
}
