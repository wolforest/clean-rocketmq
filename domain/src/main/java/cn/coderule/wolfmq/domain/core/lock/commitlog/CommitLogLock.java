package cn.coderule.wolfmq.domain.core.lock.commitlog;

/**
 * @renamed from PutMessageLock to CommitLogLock
 * Used when trying to put message
 * used by CommitLog
 */
public interface CommitLogLock {
    boolean isLocked();

    void lock();

    void unlock();
}
