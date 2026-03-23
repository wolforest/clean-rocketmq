package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.message.MessageEncoder;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLogFlushPolicy;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.minimq.domain.test.ConfigMock;
import cn.coderule.minimq.domain.test.MessageMock;
import cn.coderule.minimq.store.domain.commitlog.flush.SyncCommitLogFlushPolicy;
import cn.coderule.minimq.store.infra.file.DefaultMappedFileQueue;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCommitLogTest {
    public static int MMAP_FILE_SIZE = 1024 * 1024;

    // ==================== Existing Tests ====================

    @Test
    void testInsertAndSelectMessage(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        commitLog.assignCommitOffset(messageBO);
        assertTrue(messageBO.getCommitOffset() >= 0);

        EnqueueFuture future = commitLog.insert(messageBO);
        EnqueueResult result = future.get();
        assertNotNull(result);

        MessageBO newMessage = commitLog.select(messageBO.getCommitOffset());
        assertNotNull(newMessage);
        assertEquals(messageBO.getCommitOffset(), newMessage.getCommitOffset());
        assertEquals(messageBO.getBody().length, newMessage.getBody().length);
        assertEquals(messageBO.getTopic(), newMessage.getTopic());

        assertEquals(messageBO.getTopicLength(), newMessage.getTopicLength());
        assertEquals(messageBO.getBodyLength(), newMessage.getBodyLength());
        assertEquals(messageBO.getPropertyLength(), newMessage.getPropertyLength());
        assertEquals(messageBO.getMessageLength(), newMessage.getMessageLength());

        commitLog.destroy();
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

        EnqueueFuture future = commitLog.insert(first);
        EnqueueResult result = future.get();
        assertNotNull(result);

        MessageBO second = createMessage(encoder);
        commitLog.assignCommitOffset(second);
        assertTrue(second.getCommitOffset() >= 0);
        assertTrue(second.getCommitOffset() > first.getCommitOffset());

        long diff = second.getCommitOffset() - first.getCommitOffset();
        assertEquals(diff, first.getMessageLength());

        commitLog.destroy();
    }

    // ==================== Insert Tests ====================

    @Test
    void testInsertMultipleMessages(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        int messageCount = 10;
        long[] offsets = new long[messageCount];

        for (int i = 0; i < messageCount; i++) {
            MessageBO messageBO = createMessage(encoder);
            EnqueueFuture future = commitLog.insert(messageBO);
            EnqueueResult result = future.get();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            offsets[i] = messageBO.getCommitOffset();
        }

        // offsets should be strictly increasing
        for (int i = 1; i < messageCount; i++) {
            assertTrue(offsets[i] > offsets[i - 1]);
        }

        commitLog.destroy();
    }

    @Test
    void testInsertDifferentSizes(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        int[] bodySizes = {100, 1024, 1024 * 2, 1024 * 5};

        for (int bodySize : bodySizes) {
            MessageBO messageBO = MessageMock.createMessage("TEST_TOPIC", bodySize);
            encoder.calculate(messageBO);

            EnqueueFuture future = commitLog.insert(messageBO);
            EnqueueResult result = future.get();

            assertNotNull(result);
            assertTrue(result.isSuccess());

            // select and verify body length
            MessageBO selected = commitLog.select(messageBO.getCommitOffset());
            assertNotNull(selected);
            assertTrue(selected.isValid());
            assertEquals(bodySize, selected.getBody().length);
        }

        commitLog.destroy();
    }

    @Test
    void testInsertWithProperties(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = MessageMock.createMessage();
        messageBO.getProperties().put("KEY1", "VALUE1");
        messageBO.getProperties().put("KEY2", "VALUE2");
        encoder.calculate(messageBO);

        EnqueueFuture future = commitLog.insert(messageBO);
        EnqueueResult result = future.get();
        assertTrue(result.isSuccess());

        MessageBO selected = commitLog.select(messageBO.getCommitOffset());
        assertNotNull(selected);
        assertTrue(selected.isValid());
        assertNotNull(selected.getProperties());

        commitLog.destroy();
    }

    // ==================== Select Tests ====================

    @Test
    void testSelectWithOffsetAndSize(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        commitLog.insert(messageBO);

        // select(offset, size)
        MessageBO selected = commitLog.select(messageBO.getCommitOffset(), messageBO.getMessageLength());
        assertNotNull(selected);
        assertTrue(selected.isValid());
        assertEquals(messageBO.getTopic(), selected.getTopic());
        assertEquals(messageBO.getBody().length, selected.getBody().length);

        commitLog.destroy();
    }

    @Test
    void testSelectInvalidOffset(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);
        commitLog.insert(messageBO);

        // offset beyond written data
        MessageBO notFound = commitLog.select(MMAP_FILE_SIZE * 2L);
        assertNotNull(notFound);
        assertFalse(notFound.isValid());

        commitLog.destroy();
    }

    @Test
    void testSelectBuffer(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);
        commitLog.insert(messageBO);

        // selectBuffer(offset) - returns from offset to end of write position
        SelectedMappedBuffer buffer = commitLog.selectBuffer(messageBO.getCommitOffset());
        assertNotNull(buffer);
        assertTrue(buffer.getSize() >= messageBO.getMessageLength());
        buffer.release();

        commitLog.destroy();
    }

    @Test
    void testSelectBufferWithSize(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);
        commitLog.insert(messageBO);

        // selectBuffer(offset, size)
        SelectedMappedBuffer buffer = commitLog.selectBuffer(
            messageBO.getCommitOffset(), messageBO.getMessageLength());
        assertNotNull(buffer);
        assertEquals(messageBO.getMessageLength(), buffer.getSize());
        buffer.release();

        commitLog.destroy();
    }

    @Test
    void testSelectBufferInvalidOffset(@TempDir Path tmpDir) {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        SelectedMappedBuffer buffer = commitLog.selectBuffer(MMAP_FILE_SIZE * 2L);
        assertNull(buffer);

        commitLog.destroy();
    }

    // ==================== AssignCommitOffset Tests ====================

    @Test
    void testAssignCommitOffset_InvalidMessageLength(@TempDir Path tmpDir) {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        // messageLength defaults to -1 when not calculated
        MessageBO messageBO = MessageMock.createMessage();

        assertThrows(InvalidRequestException.class, () -> commitLog.assignCommitOffset(messageBO));

        commitLog.destroy();
    }

    @Test
    void testAssignCommitOffset_ThreeMessages(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO m1 = createMessage(encoder);
        MessageBO m2 = createMessage(encoder);
        MessageBO m3 = createMessage(encoder);

        commitLog.insert(m1);
        commitLog.insert(m2);
        commitLog.insert(m3);

        // offsets should be sequential: 0, len1, len1+len2
        assertEquals(0, m1.getCommitOffset());
        assertEquals(m1.getMessageLength(), m2.getCommitOffset());
        assertEquals(m1.getMessageLength() + m2.getMessageLength(), m3.getCommitOffset());

        commitLog.destroy();
    }

    // ==================== Offset Query Tests ====================

    @Test
    void testMinMaxOffset_EmptyCommitLog(@TempDir Path tmpDir) {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        assertEquals(0, commitLog.getMinOffset());
        assertEquals(0, commitLog.getMaxOffset());

        commitLog.destroy();
    }

    @Test
    void testMinMaxOffset_AfterInsert(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        commitLog.insert(messageBO);

        assertEquals(0, commitLog.getMinOffset());
        assertEquals(messageBO.getMessageLength(), commitLog.getMaxOffset());

        commitLog.destroy();
    }

    @Test
    void testMaxOffset_GrowsWithInserts(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());

        long prevMax = commitLog.getMaxOffset();
        for (int i = 0; i < 5; i++) {
            MessageBO messageBO = createMessage(encoder);
            commitLog.insert(messageBO);

            long newMax = commitLog.getMaxOffset();
            assertTrue(newMax > prevMax);
            prevMax = newMax;
        }

        commitLog.destroy();
    }

    // ==================== Flush Tests ====================

    @Test
    void testFlushedOffset(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        commitLog.insert(messageBO);

        // SyncCommitLogFlusher flushes immediately, so flushedOffset should advance
        long flushedOffset = commitLog.getFlushedOffset();
        assertTrue(flushedOffset > 0);
        assertEquals(flushedOffset, commitLog.getMaxOffset());

        commitLog.destroy();
    }

    @Test
    void testUnFlushedSize(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);

        commitLog.insert(messageBO);

        // with SyncCommitLogFlusher, unFlushedSize should be 0 after insert
        assertEquals(0, commitLog.getUnFlushedSize());

        commitLog.destroy();
    }

    // ==================== Raw Insert Tests ====================

    @Test
    void testRawInsertAndSelect(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        // first insert a message to create a mapped file
        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);
        commitLog.insert(messageBO);

        long maxOffset = commitLog.getMaxOffset();

        // raw insert at current max offset
        byte[] rawData = "hello raw data".getBytes();
        InsertResult rawResult = commitLog.insert(maxOffset, rawData, 0, rawData.length);
        assertNotNull(rawResult);

        commitLog.destroy();
    }

    @Test
    void testRawInsert_InvalidOffset(@TempDir Path tmpDir) {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        byte[] rawData = "test".getBytes();
        // no mapped file exists at this offset
        InsertResult result = commitLog.insert(MMAP_FILE_SIZE * 10L, rawData, 0, rawData.length);
        assertNotNull(result);
        assertFalse(result.isSuccess());

        commitLog.destroy();
    }

    // ==================== Destroy Tests ====================

    @Test
    void testDestroy(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = createMessage(encoder);
        commitLog.insert(messageBO);

        assertTrue(commitLog.getMaxOffset() > 0);

        commitLog.destroy();

        // after destroy, queue should be empty
        assertEquals(0, commitLog.getMaxOffset());
    }

    // ==================== MappedFileQueue Access ====================

    @Test
    void testGetMappedFileQueue(@TempDir Path tmpDir) {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        DefaultCommitLog commitLog = (DefaultCommitLog) createCommitLog(dir, storeConfig);

        MappedFileQueue queue = commitLog.getMappedFileQueue();
        assertNotNull(queue);

        commitLog.destroy();
    }

    // ==================== Different Topics ====================

    @Test
    void testInsertDifferentTopics(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        String[] topics = {"TOPIC_A", "TOPIC_B", "TOPIC_C"};

        for (String topic : topics) {
            MessageBO messageBO = MessageMock.createMessage(topic, 100);
            encoder.calculate(messageBO);

            EnqueueFuture future = commitLog.insert(messageBO);
            EnqueueResult result = future.get();
            assertTrue(result.isSuccess());

            MessageBO selected = commitLog.select(messageBO.getCommitOffset());
            assertNotNull(selected);
            assertTrue(selected.isValid());
            assertEquals(topic, selected.getTopic());
        }

        commitLog.destroy();
    }

    // ==================== EnqueueResult Verification ====================

    @Test
    void testEnqueueResult_Fields(@TempDir Path tmpDir) throws ExecutionException, InterruptedException {
        String dir = tmpDir.toString();
        StoreConfig storeConfig = ConfigMock.createStoreConfig(dir);
        CommitLog commitLog = createCommitLog(dir, storeConfig);

        MessageEncoder encoder = new MessageEncoder(storeConfig.getMessageConfig());
        MessageBO messageBO = MessageMock.createMessage("RESULT_TEST", 100, 3, 42);
        encoder.calculate(messageBO);

        EnqueueFuture future = commitLog.insert(messageBO);
        EnqueueResult result = future.get();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals(EnqueueStatus.PUT_OK, result.getStatus());
        assertEquals(3, result.getQueueId());
        assertEquals(42, result.getQueueOffset());
        assertTrue(result.getCommitOffset() >= 0);

        commitLog.destroy();
    }

    // ==================== Helper Methods ====================

    private CommitLog createCommitLog(String dir, StoreConfig storeConfig) {
        MappedFileQueue queue = new DefaultMappedFileQueue(dir, MMAP_FILE_SIZE);

        CommitConfig commitConfig = storeConfig.getCommitConfig();
        commitConfig.setFileSize(MMAP_FILE_SIZE);

        CommitLogFlushPolicy flusher = new SyncCommitLogFlushPolicy(queue);
        return new DefaultCommitLog(storeConfig, 0, queue, flusher);
    }

    private MessageBO createMessage(MessageEncoder encoder) {
        MessageBO messageBO = MessageMock.createMessage();

        Pair<Boolean, Set<String>> validate = MessageEncoder.validate(messageBO);
        if (!validate.getLeft()) {
            throw new IllegalArgumentException("Invalid message: " + validate.getRight());
        }

        encoder.calculate(messageBO);
        return messageBO;
    }
}
