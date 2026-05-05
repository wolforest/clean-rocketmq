package cn.coderule.wolfmq.domain.core.lock.commitlog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommitLogSpinLockTest {

    @Test
    void lock_and_unlock() {
        CommitLogSpinLock lock = new CommitLogSpinLock();
        lock.lock();
        lock.unlock();
    }

    @Test
    void lock_unlock_relock() {
        CommitLogSpinLock lock = new CommitLogSpinLock();
        lock.lock();
        lock.unlock();
        lock.lock();
        lock.unlock();
    }

    @Test
    void concurrentLockUnlock() throws InterruptedException {
        CommitLogSpinLock lock = new CommitLogSpinLock();
        int[] counter = {0};
        int threads = 10;
        int iterations = 100;

        Thread[] threadArr = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            threadArr[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    lock.lock();
                    try {
                        counter[0]++;
                    } finally {
                        lock.unlock();
                    }
                }
            });
        }

        for (Thread t : threadArr) t.start();
        for (Thread t : threadArr) t.join();

        assertEquals(threads * iterations, counter[0]);
    }
}