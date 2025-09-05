package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.message.MessageEncoder;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogFlusher;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.utils.test.ConfigMock;
import cn.coderule.minimq.domain.utils.test.MessageMock;
import cn.coderule.minimq.store.domain.commitlog.flush.SyncCommitLogFlusher;
import cn.coderule.minimq.store.infra.file.DefaultMappedFileQueue;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCommitLogTest {
    public static int MMAP_FILE_SIZE = 1024 * 1024;

    @Test
    void testInsertAndSelect(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        commitLog.assignCommitOffset(messageBO);
        assertTrue(messageBO.getCommitOffset() >= 0);

        InsertFuture future = commitLog.insert(messageBO);
        EnqueueResult result = future.get();
        assertNotNull(result);

        MessageBO newMessage = commitLog.select(messageBO.getCommitOffset());
        assertNotNull(newMessage);
        assertEquals(messageBO.getCommitOffset(), newMessage.getCommitOffset());
        assertEquals(messageBO.getBody(), newMessage.getBody());
        assertEquals(messageBO.getTopic(), newMessage.getTopic());
    }

    @Test
    void testAssignCommitOffset(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO first = createMessage(encoder);

        commitLog.assignCommitOffset(first);
        assertTrue(first.getCommitOffset() >= 0);

        InsertFuture future = commitLog.insert(first);
        EnqueueResult result = future.get();
        assertNotNull(result);

        MessageBO second = createMessage(encoder);
        commitLog.assignCommitOffset(second);
        assertTrue(second.getCommitOffset() >= 0);
        assertTrue(second.getCommitOffset() > first.getCommitOffset());

        long diff = second.getCommitOffset() - first.getCommitOffset();
        assertEquals(diff, first.getMessageLength());
    }

    private CommitLog createCommitLog(String dir, StoreConfig storeConfig) {
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        commitConfig.setFileSize(MMAP_FILE_SIZE);

        MappedFileQueue queue = new DefaultMappedFileQueue(dir, MMAP_FILE_SIZE);
        CommitLogFlusher flusher = new SyncCommitLogFlusher(queue);

        return new DefaultCommitLog(storeConfig, queue, flusher);
    }


    public MessageBO createMessage(MessageEncoder encoder) {
        MessageBO messageBO = MessageMock.createMessage();

        Pair<Boolean, Set<String>> validate = MessageEncoder.validate(messageBO);
        if (!validate.getLeft()) {
            throw new IllegalArgumentException("Invalid message: " + validate.getRight());
        }

        encoder.calculate(messageBO);
        return messageBO;
    }


}
