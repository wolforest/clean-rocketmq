package cn.coderule.minimq.store.infra.file;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import lombok.Getter;

public abstract class ReferenceResource {
    protected static final AtomicLongFieldUpdater<ReferenceResource> REF_COUNT_UPDATER =
        AtomicLongFieldUpdater.newUpdater(ReferenceResource.class, "refCount");

    protected volatile long refCount = 1;

    @Getter
    protected volatile boolean available = true;
    protected volatile boolean cleanupOver = false;
    private volatile long firstShutdownTimestamp = 0;

    public synchronized boolean hold() {
        if (!this.isAvailable()) {
            return false;
        }

        if (REF_COUNT_UPDATER.getAndIncrement(this) > 0) {
            return true;
        }

        REF_COUNT_UPDATER.getAndDecrement(this);
        return false;
    }

    public void shutdown(final long interval) {
        if (this.available) {
            this.available = false;
            this.firstShutdownTimestamp = System.currentTimeMillis();
            this.release();
            return;
        }

        if (this.getRefCount() <= 0) {
            return;
        }

        if ((System.currentTimeMillis() - this.firstShutdownTimestamp) < interval) {
            return;
        }

        REF_COUNT_UPDATER.set(this, -1000 - REF_COUNT_UPDATER.get(this));
        this.release();
    }

    public void release() {
        long value = REF_COUNT_UPDATER.decrementAndGet(this);
        if (value > 0)
            return;

        synchronized (this) {
            this.cleanupOver = this.cleanup(value);
        }
    }

    public long getRefCount() {
        return REF_COUNT_UPDATER.get(this);
    }

    public abstract boolean cleanup(final long currentRef);

    public boolean isCleanupOver() {
        return REF_COUNT_UPDATER.get(this) <= 0 && this.cleanupOver;
    }
}
