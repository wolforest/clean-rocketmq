package com.wolfmq.domain.core.lock.commitlog;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Exclusive lock implementation to put message
 */
public class CommitLogReentrantLock implements CommitLogLock {
    private final ReentrantLock lock = new ReentrantLock(); // NonFairSync

    @Override
    public boolean isLocked() {
        return lock.isLocked();
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }
}
