package com.wolf.minimq.hello.java.atomic;

import com.wolf.common.util.test.MemoryRecoder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AtomicMemoryTest {
    private static final int COUNT = 10_000_000;

    public static void main(String[] args) {
        MemoryRecoder memoryRecoder = new MemoryRecoder();

        memoryRecoder.start();
        for (int i = 0; i < COUNT; i++) {
            new VolatileObject();
        }
        memoryRecoder.record("AtomicIntegerFieldUpdater");

        for (int i = 0; i < COUNT; i++) {
            new AtomicObject();
        }
        memoryRecoder.record("AtomicInteger");

        log.info("{}", memoryRecoder);
    }

    static class AtomicObject {
        private final AtomicInteger a0 = new AtomicInteger(0);
        private final AtomicInteger a1 = new AtomicInteger(0);
        private final AtomicInteger a2 = new AtomicInteger(0);
        private final AtomicInteger a3 = new AtomicInteger(0);
        private final AtomicInteger a4 = new AtomicInteger(0);
        private final AtomicInteger a5 = new AtomicInteger(0);
        private final AtomicInteger a6 = new AtomicInteger(0);
        private final AtomicInteger a7 = new AtomicInteger(0);
        private final AtomicInteger a8 = new AtomicInteger(0);
        private final AtomicInteger a9 = new AtomicInteger(0);
    }

    static class VolatileObject {
        private volatile int a0 = 0;
        private volatile int a1 = 0;
        private volatile int a2 = 0;
        private volatile int a3 = 0;
        private volatile int a4 = 0;
        private volatile int a5 = 0;
        private volatile int a6 = 0;
        private volatile int a7 = 0;
        private volatile int a8 = 0;
        private volatile int a9 = 0;

        private static final AtomicIntegerFieldUpdater<VolatileObject> a0Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a0");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a1Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a1");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a2Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a2");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a3Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a3");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a4Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a4");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a5Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a5");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a6Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a6");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a7Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a7");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a8Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a8");
        private static final AtomicIntegerFieldUpdater<VolatileObject> a9Updater = AtomicIntegerFieldUpdater.newUpdater(VolatileObject.class, "a9");
    }
}
