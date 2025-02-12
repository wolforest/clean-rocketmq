package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.model.dto.InsertResult;
import cn.coderule.minimq.domain.model.dto.SelectedMappedBuffer;

/**
 * CommitLog APIs, for M/S
 */
public interface CommitLogService {
    SelectedMappedBuffer select(long offset);
    InsertResult insert(long offset, byte[] data, int start, int size);
}
