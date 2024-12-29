package com.wolf.minimq.domain.utils.lock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;

public class StartupLock {
    private final RandomAccessFile lockFile;
    private FileLock lock;

    public StartupLock(String filePath) {
        File file = new File(filePath);
        try {
            lockFile = new RandomAccessFile(file, "rw");
        } catch (IOException e) {
            throw new com.wolf.common.lang.exception.io.IOException(e.getMessage());
        }
    }

    public void lock() {
        try {
            lock = lockFile.getChannel().tryLock(0, 1, false);
            if (lock == null || lock.isShared() || !lock.isValid()) {
                throw new RuntimeException("Lock failed,MQ already started");
            }

            lockFile.getChannel().write(ByteBuffer.wrap("lock".getBytes(StandardCharsets.UTF_8)));
            lockFile.getChannel().force(true);
        } catch (IOException e) {
            throw new com.wolf.common.lang.exception.io.IOException(e.getMessage());
        }
    }

    public void unlock() {
        try {
            if (null != lock) {
                lock.release();
            }

            lockFile.close();
        } catch (IOException ignored) {
        }
    }
}
