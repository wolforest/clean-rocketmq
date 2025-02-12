package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.model.dto.InsertResult;
import cn.coderule.minimq.domain.model.dto.SelectedMappedBuffer;
import cn.coderule.minimq.domain.service.store.api.CommitLogService;
import cn.coderule.minimq.domain.service.store.domain.CommitLog;

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
