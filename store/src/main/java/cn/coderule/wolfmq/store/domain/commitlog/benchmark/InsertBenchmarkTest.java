package cn.coderule.wolfmq.store.domain.commitlog.benchmark;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.io.DirUtil;
import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.core.lock.queue.EnqueueLock;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.message.MessageEncoder;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLogFlushPolicy;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import cn.coderule.wolfmq.store.domain.commitlog.flush.policy.EmptyCommitLogFlushPolicy;
import cn.coderule.wolfmq.store.domain.commitlog.log.DefaultCommitLog;
import cn.coderule.wolfmq.store.infra.file.DefaultMappedFileQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InsertBenchmarkTest {
    public static int MMAP_FILE_SIZE = 1024 * 1024 * 1024;

    public static void main(String[] args) {
        try {
            singleThreadBenchmark();
            partitionPerCoreBenchmark();
            multiThreadBenchmark();
            multiThreadWithEnqueueLockBenchmark();
        } catch (Exception e) {
            log.error("main error", e);
        }
    }

    public static void singleThreadBenchmark() {
        String dir = System.getProperty("java.io.tmpdir") + "/benchmark-insert-single-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir);
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            commitLog.assignCommitOffset(messageBO);
            commitLog.insert(messageBO);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Insert 1000000 messages cost " + (endTime - startTime) + " ms");
        System.out.println("insert speed: " + 1000000 * 1000 / (endTime - startTime) + " msg/s");
        System.out.println("\n\n\n\n");

        commitLog.destroy();
        DirUtil.delete(dir);
    }

    public static void partitionPerCoreBenchmark() {
        int cpuNumber = SystemUtil.getProcessorNumber();
        String baseDir = System.getProperty("java.io.tmpdir") + "/benchmark-insert-core-" + System.currentTimeMillis();

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < cpuNumber; i++) {
            String dir = baseDir + "/commitlog" + i;
            DirUtil.createIfNotExists(dir);

            StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
            CommitLog commitLog = createCommitLog(dir, storeConfig);

            MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
            MessageBO messageBO = createMessage(encoder);

            Runnable task = () -> {
                for (int j = 0; j < 500000; j++) {
                    commitLog.insert(messageBO);
                }
            };

            Thread thread = new Thread(task);
            threads.add(thread);
        }

        long startTime = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("concurrentBenchmark exception: " + e);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("cpu number: " + cpuNumber);
        System.out.println("Insert 1000000 messages cost " + (endTime - startTime) + " ms");
        System.out.println("insert speed: " + 100000L * cpuNumber * 1000 / (endTime - startTime) + " msg/s");
        System.out.println("\n\n\n\n");

        DirUtil.delete(baseDir);
    }

    public static void multiThreadBenchmark() {
        String dir = System.getProperty("java.io.tmpdir") + "/benchmark-insert-multi-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir);
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        Runnable task = () -> {
            for (int i = 0; i < 500000; i++) {
                commitLog.insert(messageBO);
            }
        };

        int cpuNumber = SystemUtil.getProcessorNumber();
        List<Thread> threads = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < cpuNumber; i++) {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.error("multiThreadBenchmark exception", e);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("cpu number: " + cpuNumber);
        System.out.println("Insert 1000000 messages cost " + (endTime - startTime) + " ms");
        System.out.println("insert speed: " + 1000000 * 1000 / (endTime - startTime) + " msg/s");
        System.out.println("\n\n\n\n");

        commitLog.destroy();
        DirUtil.delete(dir);
    }

    public static void multiThreadWithEnqueueLockBenchmark() {
        String dir = System.getProperty("java.io.tmpdir") + "/benchmark-insert-lock-" + System.currentTimeMillis();
        DirUtil.createIfNotExists(dir);
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);
        EnqueueLock enqueueLock = new EnqueueLock();

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        Runnable task = () -> {
            for (int i = 0; i < 100000; i++) {
                String topic = getTopic();
                int queueId = getQueueId();
                enqueueLock.lock(topic, queueId);

                commitLog.assignCommitOffset(messageBO);
                commitLog.insert(messageBO);

                enqueueLock.unlock(topic, queueId);
            }
        };

        int cpuNumber = SystemUtil.getProcessorNumber();
        List<Thread> threads = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < cpuNumber; i++) {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.error("multiThreadWithEnqueueLockBenchmark exception", e);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("cpu number: " + cpuNumber);
        System.out.println("Insert 1000000 messages cost " + (endTime - startTime) + " ms");
        System.out.println("insert speed: " + 1000000 * 1000 / (endTime - startTime) + " msg/s");
        System.out.println("\n\n\n\n");

        commitLog.destroy();
        DirUtil.delete(dir);
    }

    private static String getTopic() {
        int topicIndex = ThreadLocalRandom.current().nextInt(50);
        return "topic-" + topicIndex;
    }

    private static int getQueueId() {
        return ThreadLocalRandom.current().nextInt(10);
    }

    private static MessageBO createMessage(MessageEncoder encoder) {
        MessageBO messageBO = MessageMock.createMessage(1024);

        Pair<Boolean, Set<String>> validate = MessageEncoder.validate(messageBO);
        if (!validate.getLeft()) {
            throw new IllegalArgumentException("Invalid message: " + validate.getRight());
        }

        encoder.calculate(messageBO);
        return messageBO;
    }

    private static CommitLog createCommitLog(String dir, StoreConfig storeConfig) {
        MappedFileQueue queue = new DefaultMappedFileQueue(dir, MMAP_FILE_SIZE);

        CommitConfig commitConfig = storeConfig.getCommitConfig();
        commitConfig.setFileSize(MMAP_FILE_SIZE);

        CommitLogFlushPolicy flusher = new EmptyCommitLogFlushPolicy(queue);
        return new DefaultCommitLog(storeConfig, 0, queue, flusher);
    }
}
