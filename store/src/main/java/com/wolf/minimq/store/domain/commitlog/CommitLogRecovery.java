package com.wolf.minimq.store.domain.commitlog;

import com.wolf.minimq.domain.service.store.domain.CommitLog;

public class CommitLogRecovery {
    private final CommitLog commitLog;

    public CommitLogRecovery(CommitLog commitLog) {
        this.commitLog = commitLog;
    }

    public void recover(long maxConsumeQueueOffset, boolean isNormalExit) {

    }
}
