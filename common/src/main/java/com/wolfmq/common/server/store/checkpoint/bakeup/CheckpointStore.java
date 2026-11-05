package com.wolfmq.common.server.store.checkpoint.bakeup;

import cn.coderule.common.lang.concurrent.lock.ReentrantLock;
import cn.coderule.common.lang.exception.lang.IllegalAccessException;
import java.nio.ByteBuffer;
import java.util.SortedMap;

public abstract class CheckpointStore {
    protected static int BATCH_COUNT = 4;
    protected static int BATCH_SIZE = 100;
    protected static int BUFFER_SIZE = 8 * BATCH_SIZE * BATCH_COUNT;

    protected final String fileName;
    protected final ByteBuffer buffer;
    protected int batchCount = 0;
    protected ReentrantLock lock;

    public CheckpointStore(String fileName) {
        this.fileName = fileName;
        this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.lock = new ReentrantLock();
    }

    abstract void load();
    abstract void save();

    protected CheckpointStore initBuffer() {
        lock.lock();

        buffer.clear();
        batchCount = 0;

        return this;
    }

    protected CheckpointStore append(SortedMap<Integer, Long> map) {
        int size = map.size();
        if (size < 1) {
            throw new IllegalArgumentException("[AbstractCheckpointStore]map size must be greater than 0");
        }

        map.forEach((_, v) -> buffer.putLong(v));

        int rest = BATCH_SIZE - size;
        for (int i = 0; i < rest; i++) {
            buffer.putLong(0);
        }

        batchCount++;
        return this;
    }

    protected CheckpointStore appendZero() {
        for (int i = 0; i < BATCH_SIZE; i++) {
            buffer.putLong(0);
        }

        batchCount++;
        return this;
    }

    protected void persist() {
        if (BATCH_COUNT != batchCount) {
            throw new IllegalAccessException("[AbstractCheckpointStore]ByteBuffer is not full");
        }

        buffer.flip();


        batchCount = 0;
        lock.unlock();
    }
}
