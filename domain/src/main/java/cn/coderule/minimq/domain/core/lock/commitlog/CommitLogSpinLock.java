package cn.coderule.minimq.domain.core.lock.commitlog;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spin lock Implementation to put message, suggest using this with low race conditions
 */
public class CommitLogSpinLock implements CommitLogLock {
    //true: Can lock, false : in lock.
    private final AtomicBoolean putMessageSpinLock = new AtomicBoolean(true);

    @Override
    public void lock() {
        boolean flag;
        do {
            flag = this.putMessageSpinLock.compareAndSet(true, false);
        }
        while (!flag);
    }

    @Override
    public void unlock() {
        this.putMessageSpinLock.compareAndSet(false, true);
    }
}
