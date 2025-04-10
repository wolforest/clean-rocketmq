package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.dto.InsertResult;
import cn.coderule.minimq.domain.domain.dto.SelectedMappedBuffer;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import cn.coderule.minimq.domain.service.store.domain.CommitLog;

public class CommitLogStoreImpl implements CommitLogStore {
    private CommitLog commitLog;

    public CommitLogStoreImpl(CommitLog commitLog) {
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
