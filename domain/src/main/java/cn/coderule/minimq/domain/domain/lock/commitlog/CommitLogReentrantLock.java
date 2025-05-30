package cn.coderule.minimq.domain.domain.lock.commitlog;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Exclusive lock implementation to put message
 */
public class CommitLogReentrantLock implements CommitLogLock {
    private final ReentrantLock lock = new ReentrantLock(); // NonFairSync

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }
}
