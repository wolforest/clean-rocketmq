package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.minimq.store.domain.commitlog.vo.GroupCommitRequest;

public class GroupFlushService extends FlushService {
    @Override
    public String getServiceName() {
        return GroupFlushService.class.getSimpleName();
    }

    @Override
    public void run() {

    }

    public void addRequest(GroupCommitRequest request) {

    }
}
