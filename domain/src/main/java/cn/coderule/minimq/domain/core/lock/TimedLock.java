package cn.coderule.minimq.domain.core.lock;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;

public class TimedLock {
    private final AtomicBoolean lock;
    @Getter
    private volatile long lockTime;

    public TimedLock() {
        this.lock = new AtomicBoolean(true);
        this.lockTime = System.currentTimeMillis();
    }

    public boolean tryLock() {
        boolean ret = lock.compareAndSet(true, false);
        if (!ret) {
            return false;
        }

        this.lockTime = System.currentTimeMillis();
        return true;
    }

    public void unLock() {
        lock.set(true);
    }

    public boolean isLocked() {
        return !lock.get();
    }

}
