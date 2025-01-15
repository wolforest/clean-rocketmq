package com.wolf.minimq.store.domain.commitlog.flush;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RealTimeFlushService extends FlushService {
    @Override
    public String getServiceName() {
        return RealTimeFlushService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
