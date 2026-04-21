package cn.coderule.wolfmq.store.domain.commitlog.benchmark;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.io.DirUtil;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.message.MessageEncoder;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLogFlushPolicy;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import cn.coderule.wolfmq.store.domain.commitlog.flush.policy.EmptyCommitLogFlushPolicy;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.commitlog.log.DefaultCommitLog;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import cn.coderule.wolfmq.store.infra.file.DefaultMappedFileQueue;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class CommitLogManagerBenchmarkTest {
    public static int MMAP_FILE_SIZE = 1024 * 1024 * 1024;
    public static int MESSAGE_SIZE = 1024;
    public static int WARMUP_ITERATIONS = 1000;
    public static int BENCHMARK_ITERATIONS = 5000000;

    public static void main(String[] args) {
        try {
            benchmarkSingleThread_DefaultCommitLog();
            benchmarkSingleThread_CommitLogManager_1Shard();
            benchmarkMultiThread_CommitLogManager_1Shard();
            benchmarkDefaultCommitLog_6Threads();
            benchmarkMultiThread_CommitLogManager_10Shard();
            benchmarkMultiThread_CommitLogManager_2Shard();
            benchmarkMultiThread_CommitLogManager_5Shards();
            benchmarkMultiThread_CommitLogManager_10Shards();
            benchmarkShardScaling_4Threads();
            benchmarkDefaultCommitLog_MultiThread();
            benchmarkComparison_1ShardVs2Shards();
            benchmarkComparison_1ShardVs5Shards();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void benchmarkSingleThread_DefaultCommitLog() {
        String dir = System.getProperty("java.io.tmpdir") + "/benchmark-default-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir);
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig, 0);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());

        warmup(commitLog, encoder);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            MessageBO message = createMessage(encoder, "topic-0");
            commitLog.insert(message);
        }
        long endTime = System.currentTimeMillis();

        printResult("SingleThread-DefaultCommitLog", 1, 1, BENCHMARK_ITERATIONS, endTime - startTime);

        commitLog.destroy();
        DirUtil.delete(dir);
    }

    public static void benchmarkSingleThread_CommitLogManager_1Shard() {
        String dir = System.getProperty("java.io.tmpdir") + "/benchmark-manager1-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir);
        CommitLogManager manager = createCommitLogManager(dir, 1, 1);

        MessageEncoder encoder = new MessageEncoder(createStoreConfig(dir).getMessageConfig());

        warmup(manager, encoder, 1);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            MessageBO message = createMessage(encoder, "topic-" + (i % 10));
            manager.insert(message);
        }
        long endTime = System.currentTimeMillis();

        printResult("SingleThread-CommitLogManager-1Shard", 1, 1, BENCHMARK_ITERATIONS, endTime - startTime);

        shutdown(manager);
        DirUtil.delete(dir);
    }

    public static void benchmarkMultiThread_CommitLogManager_1Shard() {
        int[] threadCounts = {1, 4, 8, 10, 12};
        int shardCount = 1;

        for (int threads : threadCounts) {
            String dir = System.getProperty("java.io.tmpdir") + "/benchmark-shard1-" + threads + "-" + System.currentTimeMillis();
            DirUtil.createIfNotExists(dir);
            CommitLogManager manager = createCommitLogManager(dir, shardCount, 1);

            MessageEncoder encoder = new MessageEncoder(createStoreConfig(dir).getMessageConfig());

            warmup(manager, encoder, shardCount);

            runMultiThreadBenchmark(manager, encoder, threads, shardCount, BENCHMARK_ITERATIONS,
                "MultiThread-CommitLogManager-1Shard-" + threads + "threads");

            shutdown(manager);
            DirUtil.delete(dir);
        }
    }

    public static void benchmarkDefaultCommitLog_6Threads() {
        int[] threadCounts = {10, 10, 10, 10, 10};

        for (int threads : threadCounts) {
            String dir = System.getProperty("java.io.tmpdir") + "/benchmark-default-" + threads + "-" + System.currentTimeMillis();
            DirUtil.createIfNotExists(dir);
            StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
            CommitLog commitLog = createCommitLog(dir, storeConfig, 0);

            MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());

            runDefaultCommitLogMultiThreadBenchmark(commitLog, encoder, threads, BENCHMARK_ITERATIONS,
                "MultiThread-DefaultCommitLog-" + threads + "threads");

            commitLog.destroy();
            DirUtil.delete(dir);
        }
    }

    public static void benchmarkMultiThread_CommitLogManager_10Shard() {
        int[] threadCounts = {10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
        int shardCount = 10;

        for (int threads : threadCounts) {
            String dir = System.getProperty("java.io.tmpdir") + "/benchmark-shard10-" + threads + "-" + System.currentTimeMillis();
            DirUtil.createIfNotExists(dir);
            CommitLogManager manager = createCommitLogManager(dir, shardCount, shardCount);

            MessageEncoder encoder = new MessageEncoder(createStoreConfig(dir).getMessageConfig());

            runMultiThreadBenchmark(manager, encoder, threads, shardCount, BENCHMARK_ITERATIONS,
                "CommitLogManager-" + shardCount + "shards");

            shutdown(manager);
            DirUtil.delete(dir);
        }
    }

    public static void benchmarkMultiThread_CommitLogManager_2Shard() {
        int[] threadCounts = {1, 4, 8, 10, 12};
        int shardCount = 2;

        for (int threads : threadCounts) {
            String dir = System.getProperty("java.io.tmpdir") + "/benchmark-shard2-" + threads + "-" + System.currentTimeMillis();
            DirUtil.createIfNotExists(dir);
            CommitLogManager manager = createCommitLogManager(dir, shardCount, shardCount);

            MessageEncoder encoder = new MessageEncoder(createStoreConfig(dir).getMessageConfig());

            warmup(manager, encoder, shardCount);

            runMultiThreadBenchmark(manager, encoder, threads, shardCount, BENCHMARK_ITERATIONS,
                "MultiThread-CommitLogManager-2Shard-" + threads + "threads");

            shutdown(manager);
            DirUtil.delete(dir);
        }
    }

    public static void benchmarkMultiThread_CommitLogManager_5Shards() {
        int[] threadCounts = {1, 4, 8, 10, 12};
        int shardCount = 5;
        int topicCount = 50;

        for (int threads : threadCounts) {
            String dir = System.getProperty("java.io.tmpdir") + "/benchmark-shard5-" + threads + "-" + System.currentTimeMillis();
            DirUtil.createIfNotExists(dir);
            CommitLogManager manager = createCommitLogManager(dir, shardCount, shardCount);

            MessageEncoder encoder = new MessageEncoder(createStoreConfig(dir).getMessageConfig());

            warmup(manager, encoder, shardCount);

            runMultiThreadBenchmarkWithTopics(manager, encoder, threads, shardCount, topicCount, BENCHMARK_ITERATIONS,
                "MultiThread-CommitLogManager-5Shards-" + threads + "threads");

            shutdown(manager);
            DirUtil.delete(dir);
        }
    }

    public static void benchmarkMultiThread_CommitLogManager_10Shards() {
        int[] threadCounts = {1, 2, 4, 8};
        int shardCount = 10;
        int topicCount = 100;

        for (int threads : threadCounts) {
            String dir = System.getProperty("java.io.tmpdir") + "/benchmark-shard10-" + threads + "-" + System.currentTimeMillis();
            DirUtil.createIfNotExists(dir);
            CommitLogManager manager = createCommitLogManager(dir, shardCount, shardCount);

            MessageEncoder encoder = new MessageEncoder(createStoreConfig(dir).getMessageConfig());

            warmup(manager, encoder, shardCount);

            runMultiThreadBenchmarkWithTopics(manager, encoder, threads, shardCount, topicCount, BENCHMARK_ITERATIONS,
                "MultiThread-CommitLogManager-10Shards-" + threads + "threads");

            shutdown(manager);
            DirUtil.delete(dir);
        }
    }

    public static void benchmarkShardScaling_4Threads() {
        int threads = 4;
        int[] shardCounts = {1, 2, 5, 10};
        int topicCount = 100;

        for (int shardCount : shardCounts) {
            String dir = System.getProperty("java.io.tmpdir") + "/benchmark-scale-" + shardCount + "-" + System.currentTimeMillis();
            DirUtil.createIfNotExists(dir);
            CommitLogManager manager = createCommitLogManager(dir, shardCount, shardCount);

            MessageEncoder encoder = new MessageEncoder(createStoreConfig(dir).getMessageConfig());

            warmup(manager, encoder, shardCount);

            runMultiThreadBenchmarkWithTopics(manager, encoder, threads, shardCount, topicCount, BENCHMARK_ITERATIONS,
                "ShardScaling-4Threads-" + shardCount + "Shards");

            shutdown(manager);
            DirUtil.delete(dir);
        }
    }

    public static void benchmarkDefaultCommitLog_MultiThread() {
        int[] threadCounts = {1, 4, 8, 10, 12};

        for (int threads : threadCounts) {
            String dir = System.getProperty("java.io.tmpdir") + "/benchmark-defaultmt-" + threads + "-" + System.currentTimeMillis();
            DirUtil.createIfNotExists(dir);
            StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
            CommitLog commitLog = createCommitLog(dir, storeConfig, 0);

            MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());

            warmup(commitLog, encoder);

            runDefaultCommitLogMultiThreadBenchmark(commitLog, encoder, threads, BENCHMARK_ITERATIONS,
                "MultiThread-DefaultCommitLog-" + threads + "threads");

            commitLog.destroy();
            DirUtil.delete(dir);
        }
    }

    public static void benchmarkComparison_1ShardVs2Shards() {
        int threads = 8;
        int topicCount = 50;

        String dir1 = System.getProperty("java.io.tmpdir") + "/benchmark-compare-1shard-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir1);
        CommitLogManager manager1 = createCommitLogManager(dir1, 1, 1);
        MessageEncoder encoder1 = new MessageEncoder(createStoreConfig(dir1).getMessageConfig());
        warmup(manager1, encoder1, 1);
        runMultiThreadBenchmarkWithTopics(manager1, encoder1, threads, 1, topicCount, BENCHMARK_ITERATIONS,
            "Compare-1Shard-8Threads");
        shutdown(manager1);
        DirUtil.delete(dir1);

        String dir5 = System.getProperty("java.io.tmpdir") + "/benchmark-compare-2shards-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir5);
        CommitLogManager manager5 = createCommitLogManager(dir5, 2, 2);
        MessageEncoder encoder5 = new MessageEncoder(createStoreConfig(dir5).getMessageConfig());
        warmup(manager5, encoder5, 2);
        runMultiThreadBenchmarkWithTopics(manager5, encoder5, threads, 2, topicCount, BENCHMARK_ITERATIONS,
            "Compare-2Shards-8Threads");
        shutdown(manager5);
        DirUtil.delete(dir5);
    }

    public static void benchmarkComparison_1ShardVs5Shards() {
        int threads = 4;
        int topicCount = 50;

        String dir1 = System.getProperty("java.io.tmpdir") + "/benchmark-compare-1shard-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir1);
        CommitLogManager manager1 = createCommitLogManager(dir1, 1, 1);
        MessageEncoder encoder1 = new MessageEncoder(createStoreConfig(dir1).getMessageConfig());
        warmup(manager1, encoder1, 1);
        runMultiThreadBenchmarkWithTopics(manager1, encoder1, threads, 1, topicCount, BENCHMARK_ITERATIONS,
            "Compare-1Shard-4Threads");
        shutdown(manager1);
        DirUtil.delete(dir1);

        String dir5 = System.getProperty("java.io.tmpdir") + "/benchmark-compare-5shards-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir5);
        CommitLogManager manager5 = createCommitLogManager(dir5, 5, 5);
        MessageEncoder encoder5 = new MessageEncoder(createStoreConfig(dir5).getMessageConfig());
        warmup(manager5, encoder5, 5);
        runMultiThreadBenchmarkWithTopics(manager5, encoder5, threads, 5, topicCount, BENCHMARK_ITERATIONS,
            "Compare-5Shards-4Threads");
        shutdown(manager5);
        DirUtil.delete(dir5);
    }

    private static void warmup(CommitLog commitLog, MessageEncoder encoder) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            MessageBO message = createMessage(encoder, "topic-0");
            commitLog.assignCommitOffset(message);
            commitLog.insert(message);
        }
    }

    private static void warmup(CommitLogManager manager, MessageEncoder encoder, int shardCount) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            MessageBO message = createMessage(encoder, "topic-" + (i % Math.max(10, shardCount * 2)));
            manager.insert(message);
        }
    }

    private static void runMultiThreadBenchmark(CommitLogManager manager, MessageEncoder encoder,
        int threadCount, int shardCount, int iterations, String name) {
        runMultiThreadBenchmark(manager, encoder, threadCount, shardCount, iterations, name, false);
    }

    private static void runMultiThreadBenchmark(CommitLogManager manager, MessageEncoder encoder,
                                        int threadCount, int shardCount, int iterations, String name, boolean bindCpu) {
        int iterationsPerThread = iterations / threadCount;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong totalTime = new AtomicLong(0);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                long start = System.currentTimeMillis();
                for (int i = 0; i < iterationsPerThread; i++) {
                    MessageBO message = createMessage(encoder, "topic-" + (i % 100));
                    EnqueueFuture future = manager.insert(message);
                    try {
                        future.get();
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                long end = System.currentTimeMillis();
                totalTime.addAndGet(end - start);
                latch.countDown();
            }, "Thread-" + t).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long totalTimeMs = totalTime.get();
        printResult(name, threadCount, shardCount, iterations, totalTimeMs);
    }

    private static void runMultiThreadBenchmarkWithTopics(CommitLogManager manager, MessageEncoder encoder,
                                                   int threadCount, int shardCount, int topicCount,
                                                   int iterations, String name) {
        int iterationsPerThread = iterations / threadCount;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong totalTime = new AtomicLong(0);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                long start = System.currentTimeMillis();
                for (int i = 0; i < iterationsPerThread; i++) {
                    String topic = "topic-" + ThreadLocalRandom.current().nextInt(topicCount);
                    MessageBO message = createMessage(encoder, topic);
                    manager.insert(message);
                }
                long end = System.currentTimeMillis();
                totalTime.addAndGet(end - start);
                latch.countDown();
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long totalTimeMs = totalTime.get();
        printResult(name, threadCount, shardCount, iterations, totalTimeMs);
    }

    private static void runDefaultCommitLogMultiThreadBenchmark(CommitLog commitLog, MessageEncoder encoder,
                                                        int threadCount, int iterations, String name) {
        int iterationsPerThread = iterations / threadCount;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong totalTime = new AtomicLong(0);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                long start = System.currentTimeMillis();
                for (int i = 0; i < iterationsPerThread; i++) {
                    MessageBO message = createMessage(encoder, "topic-0");
                    EnqueueFuture future = commitLog.insert(message);
                    try {
                        future.get();
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                long end = System.currentTimeMillis();
                totalTime.addAndGet(end - start);
                latch.countDown();
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long totalTimeMs = totalTime.get();
        printResult(name, threadCount, 1, iterations, totalTimeMs);
    }

    private static void printResult(String name, int threads, int shards, int totalMessages, long timeMs) {
        double throughput = totalMessages * 1000.0 / timeMs;
        System.out.println(name + " | threads=" + threads + " | shards=" + shards +
            " | time=" + timeMs + "ms | throughput=" + String.format("%.2f", throughput) + " msg/s");
    }

    private static CommitLogManager createCommitLogManager(String baseDir, int maxSharding, int shardCount) {
        return createCommitLogManager(baseDir, maxSharding, shardCount, false);
    }

    private static CommitLogManager createCommitLogManager(String baseDir, int maxSharding, int shardCount, boolean bindCpu) {
        StoreConfig storeConfig = createStoreConfig(baseDir);
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        commitConfig.setMaxShardingNumber(maxSharding);
        commitConfig.setShardingNumber(shardCount);

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig);
        CommitLogManager manager = new CommitLogManager(commitConfig, partitioner);

        for (int i = 0; i < shardCount; i++) {
            String dir = baseDir + "/commitlog-" + i;
            DirUtil.createIfNotExists(dir);
            CommitLog commitLog = createCommitLog(dir, storeConfig, i);
            manager.addCommitLog(commitLog);
        }

        return manager;
    }

    private static StoreConfig createStoreConfig(String dir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        storeConfig.getCommitConfig().setFileSize(MMAP_FILE_SIZE);
        return storeConfig;
    }

    private static CommitLog createCommitLog(String dir, StoreConfig storeConfig, int shardId) {
        MappedFileQueue queue = new DefaultMappedFileQueue(dir, MMAP_FILE_SIZE);
        CommitLogFlushPolicy flusher = new EmptyCommitLogFlushPolicy(queue);
        return new DefaultCommitLog(storeConfig, shardId, queue, flusher);
    }

    private static MessageBO createMessage(MessageEncoder encoder, String topic) {
        MessageBO messageBO = MessageMock.createMessage(MESSAGE_SIZE);
        messageBO.setTopic(topic);

        Pair<Boolean, Set<String>> validate = MessageEncoder.validate(messageBO);
        if (!validate.getLeft()) {
            throw new IllegalArgumentException("Invalid message: " + validate.getRight());
        }

        encoder.calculate(messageBO);
        return messageBO;
    }

    private static void shutdown(CommitLogManager manager) {
        try {
            manager.shutdown();
        } catch (Exception e) {
            System.err.println("Failed to shutdown manager: " + e);
        }
    }
}
