package com.wolfmq.store.domain.retention.cleanup.cleaner;

import com.wolfmq.store.domain.retention.cleanup.CleanContext;

public interface Cleaner {
    void clean(CleanContext context);
}
