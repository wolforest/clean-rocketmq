package cn.coderule.wolfmq.domain.core.lock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimedLockTest {

    @Test
    void tryLock_success() {
        TimedLock lock = new TimedLock();
        assertTrue(lock.tryLock());
        assertTrue(lock.isLocked());
    }

    @Test
    void tryLock_alreadyLocked() {
        TimedLock lock = new TimedLock();
        assertTrue(lock.tryLock());
        assertFalse(lock.tryLock());
    }

    @Test
    void unlock_and_relock() {
        TimedLock lock = new TimedLock();
        lock.tryLock();
        lock.unLock();
        assertFalse(lock.isLocked());
        assertTrue(lock.tryLock());
    }

    @Test
    void lockTime_updatedOnLock() throws InterruptedException {
        TimedLock lock = new TimedLock();
        long beforeLock = lock.getLockTime();
        Thread.sleep(10);
        lock.tryLock();
        long afterLock = lock.getLockTime();
        assertTrue(afterLock >= beforeLock);
    }

    @Test
    void initial_state_unlocked() {
        TimedLock lock = new TimedLock();
        assertFalse(lock.isLocked());
    }
}