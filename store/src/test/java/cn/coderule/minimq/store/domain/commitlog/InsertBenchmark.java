package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.core.lock.queue.EnqueueLock;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.message.MessageEncoder;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLogFlusher;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.test.ConfigMock;
import cn.coderule.minimq.domain.test.MessageMock;
import cn.coderule.minimq.store.domain.commitlog.flush.EmptyCommitLogFlusher;
import cn.coderule.minimq.store.infra.file.DefaultMappedFileQueue;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class InsertBenchmark {
    public static int MMAP_FILE_SIZE = 2 * 1024 * 1024;



    @Test
    void singleThreadBenchmark(@TempDir Path tmpDir) {
        String dir = tmpDir.toString();
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
    }

    @Test
    void multiThreadBenchmark(@TempDir Path tmpDir) {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        Runnable task = () -> {
            for (int i = 0; i < 100000; i++) {
                commitLog.assignCommitOffset(messageBO);
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
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("cpu number: " + cpuNumber);
        System.out.println("Insert 1000000 messages cost " + (endTime - startTime) + " ms");
        System.out.println("insert speed: " + 1000000 * 1000 / (endTime - startTime) + " msg/s");
        System.out.println("\n\n\n\n");

        commitLog.destroy();
    }

    @Test
    void multiThreadWithEnqueueLockBenchmark(@TempDir Path tmpDir) {
        String dir = tmpDir.toString();
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
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("cpu number: " + cpuNumber);
        System.out.println("Insert 1000000 messages cost " + (endTime - startTime) + " ms");
        System.out.println("insert speed: " + 1000000 * 1000 / (endTime - startTime) + " msg/s");
        System.out.println("\n\n\n\n");

        commitLog.destroy();
    }


    private String getTopic() {
        int topicIndex = ThreadLocalRandom.current().nextInt(50);
        return "topic-" + topicIndex;
    }

    private int getQueueId() {
        return ThreadLocalRandom.current().nextInt(10);
    }

    private MessageBO createMessage(MessageEncoder encoder) {
        MessageBO messageBO = MessageMock.createMessage(1024);

        Pair<Boolean, Set<String>> validate = MessageEncoder.validate(messageBO);
        if (!validate.getLeft()) {
            throw new IllegalArgumentException("Invalid message: " + validate.getRight());
        }

        encoder.calculate(messageBO);
        return messageBO;
    }

    private CommitLog createCommitLog(String dir, StoreConfig storeConfig) {
        MappedFileQueue queue = new DefaultMappedFileQueue(dir, MMAP_FILE_SIZE);

        CommitConfig commitConfig = storeConfig.getCommitConfig();
        commitConfig.setFileSize(MMAP_FILE_SIZE);

        CommitLogFlusher flusher = new EmptyCommitLogFlusher(queue);
        return new DefaultCommitLog(storeConfig, queue, flusher);
    }
}
