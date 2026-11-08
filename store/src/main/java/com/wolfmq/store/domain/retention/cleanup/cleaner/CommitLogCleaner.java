package com.wolfmq.store.domain.retention.cleanup.cleaner;

import com.wolfmq.store.domain.retention.cleanup.CleanContext;
import lombok.extern.slf4j.Slf4j;

/**
 * CommitLog cleaner, clean expired commitLog when:
 *  1. is time up
 *  2. is usage exceeds threshold
 *  3. manual
 *
 */
@Slf4j
public class CommitLogCleaner implements Cleaner {
    @Override
    public void clean(CleanContext context) {

    }
}
