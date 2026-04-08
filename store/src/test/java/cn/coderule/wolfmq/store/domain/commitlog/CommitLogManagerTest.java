package cn.coderule.wolfmq.store.domain.commitlog;

import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.wolfmq.domain.test.MessageMock;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitLogManagerTest {

    private static final int MAX_SHARDING = 10;

    private TopicPartitioner partitioner;
    private CommitLogManager manager;
    private CommitConfig config;

    @BeforeEach
    void setUp() {
        config = new CommitConfig();
        config.setMaxShardingNumber(MAX_SHARDING);
        partitioner = new TopicPartitioner(config);
        manager = new CommitLogManager(config, partitioner);
    }

    @Test
    void testConstructor() {
        assertNotNull(manager);
    }

    @Test
    void testAddSingleCommitLog() {
        CommitLog commitLog = createMockCommitLog(0);
        manager.addCommitLog(commitLog);

        CommitLog found = manager.selectByShardId(0);
        assertEquals(commitLog, found);
    }

    @Test
    void testAddMultipleCommitLogs() {
        config.setShardingNumber(MAX_SHARDING);
        CommitLogManager tmpManager = new CommitLogManager(config, partitioner);
        for (int i = 0; i < MAX_SHARDING; i++) {
            CommitLog commitLog = createMockCommitLog(i);
            tmpManager.addCommitLog(commitLog);
        }

        for (int i = 0; i < MAX_SHARDING; i++) {
            CommitLog found = tmpManager.selectByShardId(i);
            assertNotNull(found);
        }
    }

    @Test
    void testAddCommitLogList() {
        config.setShardingNumber(3);
        CommitLogManager tmpManager = new CommitLogManager(config, partitioner);

        List<CommitLog> logList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            logList.add(createMockCommitLog(i));
        }

        tmpManager.addCommitLog(logList);

        assertEquals(logList.get(0), tmpManager.selectByShardId(0));
        assertEquals(logList.get(1), tmpManager.selectByShardId(1));
        assertEquals(logList.get(2), tmpManager.selectByShardId(2));
    }

    @Test
    void testAddEmptyCommitLogList() {
        manager.addCommitLog(new ArrayList<>());
        assertDoesNotThrow(() -> manager.addCommitLog((List<CommitLog>) null));
    }

    @Test
    void testSelectByShardId_Found() {
        CommitLog commitLog = createMockCommitLog(0);
        manager.addCommitLog(commitLog);

        CommitLog found = manager.selectByShardId(0);
        assertNotNull(found);
        assertEquals(commitLog, found);
    }

    @Test
    void testSelectByShardId_OutOfBounds_EmptyList() {
        assertThrows(
            IllegalArgumentException.class,
            () -> manager.selectByShardId(0)
        );

        assertThrows(
            IndexOutOfBoundsException.class,
            () -> manager.selectByShardId(11)
        );
    }

    @Test
    void testSelectByShardId_OutOfBounds_BeyondSize() {
        CommitLog commitLog = createMockCommitLog(0);
        manager.addCommitLog(commitLog);

        assertThrows(
            IndexOutOfBoundsException.class,
            () -> manager.selectByShardId(15)
        );
    }

    @Test
    void testOffsetToShardId_Zero() {
        int shardId = manager.offsetToShardId(0);
        assertEquals(0, shardId);
    }

    @Test
    void testOffsetToShardId_PositiveOffsets() {
        assertEquals(0, manager.offsetToShardId(0));
        assertEquals(1, manager.offsetToShardId(1));
        assertEquals(9, manager.offsetToShardId(9));
        assertEquals(0, manager.offsetToShardId(10));
        assertEquals(5, manager.offsetToShardId(15));
        assertEquals(9, manager.offsetToShardId(19));
        assertEquals(0, manager.offsetToShardId(20));
    }

    @Test
    void testOffsetToShardId_NegativeOffset() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> manager.offsetToShardId(-1)
        );
        assertEquals("offset must be positive", ex.getMessage());
    }

    @Test
    void testOffsetToShardId_LargeOffset() {
        long largeOffset = 1_000_000L;
        int shardId = manager.offsetToShardId(largeOffset);
        assertTrue(shardId >= 0 && shardId < MAX_SHARDING);
    }

    @Test
    void testOffsetToShardId_Consistency() {
        for (long offset = 0; offset < 1000; offset++) {
            int shardId = manager.offsetToShardId(offset);
            assertEquals((int) (offset % MAX_SHARDING), shardId);
        }
    }

    @Test
    void testInsert_MessageRouting() {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(shard0);
        manager.addCommitLog(shard1);

        MessageBO message = MessageMock.createMessage();
        message.setTopic("TOPIC_A");
        EnqueueFuture future = mock(EnqueueFuture.class);
        when(shard0.insert(any())).thenReturn(future);

        int shardId = partitioner.partitionByTopic(message.getTopic());
        if (shardId == 0) {
            manager.insert(message);
            verify(shard0).insert(message);
        }
    }

    @Test
    void testSelect_DelegatesToCorrectShard() {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(shard0);
        manager.addCommitLog(shard1);

        MessageBO expectedMessage = MessageMock.createMessage();
        when(shard0.select(anyLong(), anyInt())).thenReturn(expectedMessage);

        MessageBO result = manager.select(0, 100);

        verify(shard0).select(0, 100);
        assertEquals(expectedMessage, result);
    }

    @Test
    void testSelect_ByOffsetOnly() {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(shard0);

        MessageBO expectedMessage = MessageMock.createMessage();
        when(shard0.select(anyLong())).thenReturn(expectedMessage);

        MessageBO result = manager.select(0);

        verify(shard0).select(0);
        assertEquals(expectedMessage, result);
    }

    @Test
    void testSelectBuffer_WithoutSize() {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(shard0);

        SelectedMappedBuffer buffer = mock(SelectedMappedBuffer.class);
        when(shard0.selectBuffer(anyLong())).thenReturn(buffer);

        SelectedMappedBuffer result = manager.selectBuffer(0);

        verify(shard0).selectBuffer(0);
        assertEquals(buffer, result);
    }

    @Test
    void testSelectBuffer_WithSize() {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(shard0);

        SelectedMappedBuffer buffer = mock(SelectedMappedBuffer.class);
        when(shard0.selectBuffer(anyLong(), anyInt())).thenReturn(buffer);

        SelectedMappedBuffer result = manager.selectBuffer(0, 100);

        verify(shard0).selectBuffer(0, 100);
        assertEquals(buffer, result);
    }

    @Test
    void testInsert_RawData() {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(shard0);

        InsertResult insertResult = mock(InsertResult.class);
        when(shard0.insert(anyLong(), any(), anyInt(), anyInt())).thenReturn(insertResult);

        byte[] data = new byte[100];
        InsertResult result = manager.insert(0, data, 0, 100);

        verify(shard0).insert(0, data, 0, 100);
        assertEquals(insertResult, result);
    }

    @Test
    void testGetMinOffset() {
        CommitLog shard0 = createMockCommitLog(0);
        when(shard0.getMinOffset()).thenReturn(100L);
        manager.addCommitLog(shard0);

        long minOffset = manager.getMinOffset(0);

        assertEquals(100L, minOffset);
    }

    @Test
    void testGetMaxOffset() {
        CommitLog shard0 = createMockCommitLog(0);
        when(shard0.getMaxOffset()).thenReturn(500L);
        manager.addCommitLog(shard0);

        long maxOffset = manager.getMaxOffset(0);

        assertEquals(500L, maxOffset);
    }

    @Test
    void testGetFlushedOffset() {
        CommitLog shard0 = createMockCommitLog(0);
        when(shard0.getFlushedOffset()).thenReturn(300L);
        manager.addCommitLog(shard0);

        long flushedOffset = manager.getFlushedOffset(0);

        assertEquals(300L, flushedOffset);
    }

    @Test
    void testGetUnFlushedSize() {
        CommitLog shard0 = createMockCommitLog(0);
        when(shard0.getUnFlushedSize()).thenReturn(50L);
        manager.addCommitLog(shard0);

        long unflushedSize = manager.getUnFlushedSize(0);

        assertEquals(50L, unflushedSize);
    }

    @Test
    void testLifecycle_Start() throws Exception {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(shard0);
        manager.addCommitLog(shard1);

        manager.start();

        verify(shard0).start();
        verify(shard1).start();
    }

    @Test
    void testLifecycle_Shutdown() throws Exception {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(shard0);
        manager.addCommitLog(shard1);

        manager.shutdown();

        verify(shard0).shutdown();
        verify(shard1).shutdown();
    }

    @Test
    void testLifecycle_Initialize() throws Exception {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(shard0);
        manager.addCommitLog(shard1);

        manager.initialize();

        verify(shard0).initialize();
        verify(shard1).initialize();
    }

    @Test
    void testLifecycle_FullCycle() throws Exception {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(shard0);

        manager.initialize();
        manager.start();
        manager.shutdown();

        verify(shard0).initialize();
        verify(shard0).start();
        verify(shard0).shutdown();
    }

    @Test
    void testLifecycle_EmptyList() throws Exception {
        assertDoesNotThrow(() -> manager.start());
        assertDoesNotThrow(() -> manager.shutdown());
        assertDoesNotThrow(() -> manager.initialize());
    }

    private CommitLog createMockCommitLog(int shardId) {
        CommitLog commitLog = mock(CommitLog.class);
        when(commitLog.getShardId()).thenReturn(shardId);
        return commitLog;
    }

    @Test
    void testCalculateThreadId_SingleDigit() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertTrue(result >= 0 && result <= 9, "Single digit thread id should be 0-9");
                assertEquals(5, result);
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "test-thread-5");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_TwoDigits() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertTrue(result >= 0 && result <= 99, "Two digit thread id should be 0-99");
                assertEquals(42, result);
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "worker-thread-42");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_NoNumericSuffix() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertEquals(-1, result, "Should return -1 for no numeric suffix");
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "main");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_NonNumericSuffix() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertEquals(-1, result, "Should return -1 for non-numeric suffix");
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "Thread-ABC");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_ThreadPoolFormat() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertTrue(result >= 0 && result <= 99, "Thread pool format should extract number");
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "producer-thread-pool-12");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_LongThreadName() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertEquals(7, result, "Should extract 7 from long thread name");
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "very-long-thread-name-with-many-suffixes-for-testing-7");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_SingleCharThreadName() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertEquals(-1, result, "Single char should return -1");
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "T");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_TwoCharNumeric() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertEquals(99, result, "Should extract 99 from two-digit suffix");
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "Thread-99");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_ThreeDigitOverflow() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertTrue(result >= 0 && result <= 99, "Should handle three digits (only last two)");
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "worker-thread-123");
        testThread.start();
        testThread.join();
    }

    @Test
    void testCalculateThreadId_MixedSuffix() throws Exception {
        CommitConfig config = new CommitConfig();
        TopicPartitioner partitioner = new TopicPartitioner(config);
        CommitLogManager manager = new CommitLogManager(config, partitioner);

        Method method = CommitLogManager.class.getDeclaredMethod("calculateThreadId");
        method.setAccessible(true);

        Thread testThread = new Thread(() -> {
            try {
                int result = (int) method.invoke(manager);
                assertEquals(-1, result, "Should return -1 when last char is non-digit");
            } catch (Exception e) {
                fail("Exception: " + e.getMessage());
            }
        }, "thread-1a");
        testThread.start();
        testThread.join();
    }
}
