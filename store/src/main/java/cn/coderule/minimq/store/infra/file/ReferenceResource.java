/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
