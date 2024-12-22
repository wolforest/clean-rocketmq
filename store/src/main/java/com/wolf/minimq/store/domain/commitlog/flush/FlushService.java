package com.wolf.minimq.store.domain.commitlog.flush;

import com.wolf.common.lang.concurrent.ServiceThread;

public abstract class FlushService extends ServiceThread {
    protected static final int RETRY_TIMES = 10;
}
