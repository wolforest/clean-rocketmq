package cn.coderule.wolfmq.hello.java.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public final class ConcurrentLinkedQueueBenchmark {
    private static final int DEFAULT_WARMUP_ROUNDS = 2;
    private static final int DEFAULT_MEASURE_ROUNDS = 3;
    private static final int DEFAULT_OPS_PER_PRODUCER = 1_000_000;
    private static final int DEFAULT_PRODUCERS = 5;
    private static final int DEFAULT_CONSUMERS = 10;

    public static void main(String[] args) throws Exception {
        int warmupRounds = intArg(args, 0, DEFAULT_WARMUP_ROUNDS);
        int measureRounds = intArg(args, 1, DEFAULT_MEASURE_ROUNDS);
        int producers = intArg(args, 2, DEFAULT_PRODUCERS);
        int consumers = intArg(args, 3, DEFAULT_CONSUMERS);
        int opsPerProducer = intArg(args, 4, DEFAULT_OPS_PER_PRODUCER);

        System.out.println("ConcurrentLinkedQueue benchmark");
        System.out.println("warmupRounds=" + warmupRounds
                + ", measureRounds=" + measureRounds
                + ", producers=" + producers
                + ", consumers=" + consumers
                + ", opsPerProducer=" + opsPerProducer);
        System.out.println();

        runSingleThread(warmupRounds, measureRounds, opsPerProducer);
        System.out.println();
        runMpmc(warmupRounds, measureRounds, producers, consumers, opsPerProducer);
    }

    private static void runSingleThread(int warmupRounds, int measureRounds, int ops) {
        for (int i = 0; i < warmupRounds; i++) {
            singleThreadOnce(ops);
        }

        long best = Long.MAX_VALUE;
        for (int i = 0; i < measureRounds; i++) {
            long nanos = singleThreadOnce(ops);
            best = Math.min(best, nanos);
            printResult("single-thread", ops, nanos);
        }
        printBest("single-thread", ops, best);
    }

    private static long singleThreadOnce(int ops) {
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            queue.offer(i);
            Integer v = queue.poll();
            if (v == null) {
                throw new IllegalStateException("poll returned null");
            }
        }
        return System.nanoTime() - start;
    }

    private static void runMpmc(int warmupRounds, int measureRounds,
                               int producers, int consumers, int opsPerProducer) throws Exception {
        for (int i = 0; i < warmupRounds; i++) {
            mpmcOnce(producers, consumers, opsPerProducer, true);
        }

        long best = Long.MAX_VALUE;
        long totalOps = (long) producers * opsPerProducer;
        for (int i = 0; i < measureRounds; i++) {
            long nanos = mpmcOnce(producers, consumers, opsPerProducer, false);
            best = Math.min(best, nanos);
            printResult("mpmc", totalOps, nanos);
        }
        printBest("mpmc", totalOps, best);
    }

    private static long mpmcOnce(int producers, int consumers, int opsPerProducer, boolean warmup) throws Exception {
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(producers + consumers);
        AtomicLong remaining = new AtomicLong((long) producers * opsPerProducer);

        for (int i = 0; i < producers; i++) {
            Thread t = new Thread(() -> {
                await(start);
                for (int j = 0; j < opsPerProducer; j++) {
                    queue.offer(j);
                }
                done.countDown();
            }, "clq-producer-" + i);
            t.setDaemon(true);
            t.start();
        }

        for (int i = 0; i < consumers; i++) {
            Thread t = new Thread(() -> {
                await(start);
                long spins = 0;
                while (remaining.get() > 0) {
                    Integer v = queue.poll();
                    if (v != null) {
                        remaining.decrementAndGet();
                        spins = 0;
                    } else {
                        spins++;
                        if ((spins & 0x3FF) == 0) {
                            LockSupport.parkNanos(1_000);
                        }
                    }
                }
                done.countDown();
            }, "clq-consumer-" + i);
            t.setDaemon(true);
            t.start();
        }

        long startNs = System.nanoTime();
        start.countDown();
        done.await();
        long nanos = System.nanoTime() - startNs;

        if (!warmup && remaining.get() != 0) {
            throw new IllegalStateException("remaining=" + remaining.get());
        }
        return nanos;
    }

    private static void printResult(String name, long ops, long nanos) {
        double seconds = nanos / 1_000_000_000.0;
        double opsPerSec = ops / seconds;
        System.out.printf("%s: %,d ops in %.3f s (%.2f ops/s)%n",
                name, ops, seconds, opsPerSec);
    }

    private static void printBest(String name, long ops, long nanos) {
        double seconds = nanos / 1_000_000_000.0;
        double opsPerSec = ops / seconds;
        System.out.printf("%s best: %,d ops in %.3f s (%.2f ops/s)%n",
                name, ops, seconds, opsPerSec);
    }

    private static int intArg(String[] args, int index, int defaultValue) {
        if (args.length <= index) {
            return defaultValue;
        }
        return Integer.parseInt(args[index]);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
