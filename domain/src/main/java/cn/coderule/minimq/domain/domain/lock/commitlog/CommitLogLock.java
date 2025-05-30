package cn.coderule.minimq.domain.domain.lock.commitlog;

/**
 * @renamed from PutMessageLock to CommitLogLock
 * Used when trying to put message
 * used by CommitLog
 */
public interface CommitLogLock {
    void lock();

    void unlock();
}
