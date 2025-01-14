package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.minimq.store.domain.commitlog.vo.GroupCommitRequest;

public class GroupCommitService extends FlushService {
    @Override
    public String getServiceName() {
        return GroupCommitService.class.getSimpleName();
    }

    @Override
    public void run() {

    }

    public void addRequest(GroupCommitRequest request) {

    }
}
