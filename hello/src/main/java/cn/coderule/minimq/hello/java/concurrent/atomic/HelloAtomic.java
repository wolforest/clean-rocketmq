package cn.coderule.minimq.hello.java.concurrent.atomic;

import cn.coderule.common.util.test.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloAtomic {
    private static final int THREAD_NUM = 1;
    private static final int TASK_NUM = 100_000_000;

    private static final AtomicIntegerFieldUpdater<HelloAtomic> INT_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HelloAtomic.class, "volatileInt");

    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    private volatile int volatileInt = 0;

    public void increaseAtomic() {
        atomicInteger.incrementAndGet();
    }

    public void increaseVolatile() {
        INT_UPDATER.incrementAndGet(this);
    }

    public static void main(String[] args) {
        HelloAtomic helloAtomic = new HelloAtomic();

        Timer timer = new Timer();
        timer.start();

        testAtomic(helloAtomic);
        timer.record("Atomic");

        testVolatile(helloAtomic);
        timer.record("Volatile");

        log.info("{}", timer);
    }

    public static void testAtomic(HelloAtomic helloAtomic) {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_NUM; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < TASK_NUM; j++) {
                    helloAtomic.increaseAtomic();
                }
            });
            threads.add(thread);
        }

        threads.forEach(Thread::start);

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void testVolatile(HelloAtomic helloAtomic) {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_NUM; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < TASK_NUM; j++) {
                    helloAtomic.increaseVolatile();
                }
            });
            threads.add(thread);
        }

        threads.forEach(Thread::start);

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
