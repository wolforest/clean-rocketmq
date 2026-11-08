package com.wolfmq.store.domain.retention.cleanup.cleaner;

import com.wolfmq.store.domain.retention.cleanup.CleanContext;

/**
 * file consume queue cleaner, clean when the minOffset of commitLog changed.
 * Since CommitLog has more than one shard, The consume queue file will be deleted if:
 *     the last commitOffset < minCommitOffset of related shard
 */
public class FileConsumeQueueCleaner implements Cleaner {
    @Override
    public void clean(CleanContext context) {

    }
}
