package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.minimq.store.domain.commitlog.vo.GroupCommitRequest;

public class RealTimeFlushService extends FlushService {
    @Override
    public String getServiceName() {
        return RealTimeFlushService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
