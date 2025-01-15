package com.wolf.minimq.store.api;

import com.wolf.minimq.domain.model.dto.InsertResult;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import com.wolf.minimq.domain.service.store.api.CommitLogService;
import com.wolf.minimq.domain.service.store.domain.CommitLog;

public class CommitLogServiceImpl implements CommitLogService {
    private CommitLog commitLog;

    public CommitLogServiceImpl(CommitLog commitLog) {
        this.commitLog = commitLog;
    }
    @Override
    public SelectedMappedBuffer select(long offset) {
        return commitLog.selectBuffer(offset);
    }

    @Override
    public InsertResult insert(long offset, byte[] data, int start, int size) {
        return commitLog.insert(offset, data, start, size);
    }
}
