package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.minimq.domain.config.CommitLogConfig;
import com.wolf.minimq.domain.model.checkpoint.CheckPoint;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import com.wolf.minimq.store.domain.commitlog.vo.GroupCommitRequest;

public class IntervalCommitter extends Flusher {
    private static final long FLUSH_JOIN_TIME = 5 * 60 * 1000;

    private final CommitLogConfig commitLogConfig;
    private final MappedFileQueue mappedFileQueue;
    private final CheckPoint checkPoint;

    public IntervalCommitter(
        CommitLogConfig commitLogConfig,
        MappedFileQueue mappedFileQueue,
        CheckPoint checkPoint) {

        this.commitLogConfig = commitLogConfig;
        this.mappedFileQueue = mappedFileQueue;
        this.checkPoint = checkPoint;
    }

    @Override
    public String getServiceName() {
        return IntervalCommitter.class.getSimpleName();
    }

    @Override
    public long getJoinTime() {
        return FLUSH_JOIN_TIME;
    }

    @Override
    void addRequest(GroupCommitRequest request) {
    }

    @Override
    public void run() {

    }


}
