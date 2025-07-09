package cn.coderule.minimq.store.server.ha.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WakeupCoordinator {

    /**
     * threadId -> notifyFlag
     */
    private final ConcurrentMap<Long, AtomicBoolean> waitingMap;

    private final AtomicBoolean notifyFlag;

    public WakeupCoordinator() {
        this.notifyFlag = new AtomicBoolean(false);
        this.waitingMap = new ConcurrentHashMap<>(16);
    }

    public void wakeup() {
        boolean needNotify = notifyFlag.compareAndSet(false, true);
        if (!needNotify) {
            return;
        }

        synchronized (this) {
            this.notify();
        }
    }

    public void wakeupAll() {
        boolean needNotify = false;
        for (Map.Entry<Long, AtomicBoolean> entry : this.waitingMap.entrySet()) {
            if (entry.getValue().compareAndSet(false, true)) {
                needNotify = true;
            }
        }

        if (needNotify) {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    public void await(long interval) {
        if (this.notifyFlag.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }

        synchronized (this) {
            try {
                if (this.notifyFlag.compareAndSet(true, false)) {
                    this.onWaitEnd();
                    return;
                }
                this.wait(interval);

            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            } finally {
                this.notifyFlag.set(false);
                this.onWaitEnd();
            }
        }
    }

    public void awaitAll(long interval) {
        long currentThreadId = Thread.currentThread().getId();
        AtomicBoolean notified = this.waitingMap.computeIfAbsent(
            currentThreadId,
            k -> new AtomicBoolean(false)
        );
        if (notified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }
        synchronized (this) {
            try {
                if (notified.compareAndSet(true, false)) {
                    this.onWaitEnd();
                    return;
                }
                this.wait(interval);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            } finally {
                notified.set(false);
                this.onWaitEnd();
            }
        }
    }

    public void removeCurrentThread() {
        long currentThreadId = Thread.currentThread().getId();
        synchronized (this) {
            this.waitingMap.remove(currentThreadId);
        }
    }

    protected void onWaitEnd() {
    }
}
