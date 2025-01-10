package com.wolf.minimq.store.domain.commitlog;

import com.wolf.minimq.domain.model.checkpoint.CheckPoint;
import com.wolf.minimq.domain.service.store.domain.CommitLog;

public class CommitLogRecovery {
    private final CommitLog commitLog;
    private final CheckPoint checkPoint;

    public CommitLogRecovery(CommitLog commitLog, CheckPoint checkPoint) {
        this.commitLog = commitLog;
        this.checkPoint = checkPoint;
    }

    public void recover() {

    }
}
