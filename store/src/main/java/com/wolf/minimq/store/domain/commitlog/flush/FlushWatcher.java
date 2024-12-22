package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.common.lang.concurrent.ServiceThread;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlushWatcher extends ServiceThread {
    @Override
    public String getServiceName() {
        return FlushWatcher.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
