package com.wolfmq.store.domain.commitlog.file;

import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueFuture;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.core.mmap.SelectedMappedBuffer;
import com.wolfmq.common.testing.MessageMock;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitLogFacadeTest {

    private static final int MAX_SHARDING = 10;

    private CommitLogManager manager;
    private CommitLogFacade facade;
    private CommitConfig config;

    @BeforeEach
    void setUp() {
        config = new CommitConfig();
        config.setMaxShardingNumber(MAX_SHARDING);
        config.setShardingNumber(MAX_SHARDING);

        // mock the factory so the manager constructor doesn't touch the disk
        CommitLogFactory factory = mock(CommitLogFactory.class);
        manager = new CommitLogManager(config, factory);
        facade = new CommitLogFacade(manager);
    }

    @Test
    void testConstructor() {
        assertNotNull(manager);
        assertNotNull(facade);
        assertSame(manager, facade.getManager());
        assertEquals(0, facade.getOffsetCodec().getShardId());
    }

    @Test
    void testAddSingleCommitLog() {
        CommitLog commitLog = createMockCommitLog(0);
        manager.addCommitLog(0, commitLog);

        CommitLog found = manager.selectByShard(0);
        assertEquals(commitLog, found);
    }

    @Test
    void testAddMultipleCommitLogs() {
        CommitLogManager tmpManager = new CommitLogManager(config, mock(CommitLogFactory.class));
        for (int i = 0; i < MAX_SHARDING; i++) {
            CommitLog commitLog = createMockCommitLog(i);
            tmpManager.addCommitLog(i, commitLog);
        }

        for (int i = 0; i < MAX_SHARDING; i++) {
            CommitLog found = tmpManager.selectByShard(i);
            assertNotNull(found);
        }
    }

    @Test
    void testAddCommitLogsByIndex() {
        CommitLogManager tmpManager = new CommitLogManager(config, mock(CommitLogFactory.class));

        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        CommitLog shard2 = createMockCommitLog(2);

        tmpManager.addCommitLog(0, shard0);
        tmpManager.addCommitLog(1, shard1);
        tmpManager.addCommitLog(2, shard2);

        assertEquals(shard0, tmpManager.selectByShard(0));
        assertEquals(shard1, tmpManager.selectByShard(1));
        assertEquals(shard2, tmpManager.selectByShard(2));
    }

    @Test
    void testSelectByShardId_Found() {
        CommitLog commitLog = createMockCommitLog(0);
        manager.addCommitLog(0, commitLog);

        CommitLog found = manager.selectByShard(0);
        assertNotNull(found);
        assertEquals(commitLog, found);
    }

    @Test
    void testSelectByShardId_OutOfBounds_EmptyList() {
        assertThrows(
            IllegalArgumentException.class,
            () -> manager.selectByShard(0)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> manager.selectByShard(MAX_SHARDING + 1)
        );
    }

    @Test
    void testSelectByShardId_OutOfBounds_BeyondSize() {
        CommitLog commitLog = createMockCommitLog(0);
        manager.addCommitLog(0, commitLog);

        assertThrows(
            IllegalArgumentException.class,
            () -> manager.selectByShard(15)
        );
    }

    @Test
    void testOffsetToShardId_Zero() {
        int shardId = manager.getOffsetCodec().shardIdOf(0);
        assertEquals(0, shardId);
    }

    @Test
    void testOffsetToShardId_PositiveOffsets() {
        assertEquals(0, manager.getOffsetCodec().shardIdOf(0));
        assertEquals(1, manager.getOffsetCodec().shardIdOf(1));
        assertEquals(9, manager.getOffsetCodec().shardIdOf(9));
        assertEquals(0, manager.getOffsetCodec().shardIdOf(10));
        assertEquals(5, manager.getOffsetCodec().shardIdOf(15));
        assertEquals(9, manager.getOffsetCodec().shardIdOf(19));
        assertEquals(0, manager.getOffsetCodec().shardIdOf(20));
    }

    @Test
    void testOffsetToShardId_NegativeOffset() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> manager.getOffsetCodec().shardIdOf(-1)
        );
        assertEquals("offset can't be negative", ex.getMessage());
    }

    @Test
    void testOffsetToShardId_LargeOffset() {
        long largeOffset = 1_000_000L;
        int shardId = manager.getOffsetCodec().shardIdOf(largeOffset);
        assertTrue(shardId >= 0 && shardId < MAX_SHARDING);
    }

    @Test
    void testOffsetToShardId_Consistency() {
        for (long offset = 0; offset < 1000; offset++) {
            int shardId = manager.getOffsetCodec().shardIdOf(offset);
            assertEquals((int) (offset % MAX_SHARDING), shardId);
        }
    }

    @Test
    void testInsert_MessageRouting() {
        // single shard so random routing always selects shard 0
        CommitConfig singleConfig = new CommitConfig();
        singleConfig.setMaxShardingNumber(MAX_SHARDING);
        singleConfig.setShardingNumber(1);

        CommitLogManager singleManager = new CommitLogManager(singleConfig, mock(CommitLogFactory.class));
        CommitLogFacade singleFacade = new CommitLogFacade(singleManager);

        CommitLog shard0 = createMockCommitLog(0);
        singleManager.addCommitLog(0, shard0);

        MessageBO message = MessageMock.createMessage();
        message.setTopic("TOPIC_A");
        EnqueueFuture future = mock(EnqueueFuture.class);
        when(shard0.insert(any(MessageBO.class))).thenReturn(future);

        EnqueueFuture result = singleFacade.insert(message);

        verify(shard0).insert(message);
        assertEquals(future, result);
    }

    @Test
    void testSelect_DelegatesToCorrectShard() {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(0, shard0);
        manager.addCommitLog(1, shard1);

        MessageBO expectedMessage = MessageMock.createMessage();
        when(shard0.select(anyLong(), anyInt())).thenReturn(expectedMessage);

        MessageBO result = facade.select(0, 100);

        verify(shard0).select(0, 100);
        assertEquals(expectedMessage, result);
    }

    @Test
    void testSelect_ByOffsetOnly() {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(0, shard0);

        MessageBO expectedMessage = MessageMock.createMessage();
        when(shard0.select(anyLong())).thenReturn(expectedMessage);

        MessageBO result = facade.select(0);

        verify(shard0).select(0);
        assertEquals(expectedMessage, result);
    }

    @Test
    void testSelectBuffer_WithoutSize() {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(0, shard0);

        SelectedMappedBuffer buffer = mock(SelectedMappedBuffer.class);
        when(shard0.selectBuffer(anyLong())).thenReturn(buffer);

        SelectedMappedBuffer result = facade.selectBuffer(0);

        verify(shard0).selectBuffer(0);
        assertEquals(buffer, result);
    }

    @Test
    void testSelectBuffer_WithSize() {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(0, shard0);

        SelectedMappedBuffer buffer = mock(SelectedMappedBuffer.class);
        when(shard0.selectBuffer(anyLong(), anyInt())).thenReturn(buffer);

        SelectedMappedBuffer result = facade.selectBuffer(0, 100);

        verify(shard0).selectBuffer(0, 100);
        assertEquals(buffer, result);
    }

    @Test
    void testInsert_RawData() {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(0, shard0);

        InsertResult insertResult = mock(InsertResult.class);
        when(shard0.insert(anyLong(), any(), anyInt(), anyInt())).thenReturn(insertResult);

        byte[] data = new byte[100];
        InsertResult result = facade.insert(0, data, 0, 100);

        verify(shard0).insert(0, data, 0, 100);
        assertEquals(insertResult, result);
    }

    @Test
    void testGetMinOffset() {
        CommitLog shard0 = createMockCommitLog(0);
        when(shard0.getMinOffset()).thenReturn(100L);
        manager.addCommitLog(0, shard0);

        long minOffset = facade.getMinOffset(0);

        assertEquals(manager.getOffsetCodec().encode(100L), minOffset);
    }

    @Test
    void testGetMaxOffset() {
        CommitLog shard0 = createMockCommitLog(0);
        when(shard0.getMaxOffset()).thenReturn(500L);
        manager.addCommitLog(0, shard0);

        long maxOffset = facade.getMaxOffset(0);

        assertEquals(manager.getOffsetCodec().encode(500L), maxOffset);
    }

    @Test
    void testGetFlushedPosition() {
        CommitLog shard0 = createMockCommitLog(0);
        when(shard0.getFlushedPosition()).thenReturn(300L);
        manager.addCommitLog(0, shard0);

        long flushedOffset = facade.getFlushedOffset(0);

        assertEquals(300L, flushedOffset);
    }

    @Test
    void testGetUnFlushedSize() {
        CommitLog shard0 = createMockCommitLog(0);
        when(shard0.getUnFlushedSize()).thenReturn(50L);
        manager.addCommitLog(0, shard0);

        long unflushedSize = facade.getUnFlushedSize(0);

        assertEquals(50L, unflushedSize);
    }

    @Test
    void testLifecycle_Start() throws Exception {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(0, shard0);
        manager.addCommitLog(1, shard1);

        manager.start();

        verify(shard0).start();
        verify(shard1).start();
    }

    @Test
    void testLifecycle_Shutdown() throws Exception {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(0, shard0);
        manager.addCommitLog(1, shard1);

        manager.shutdown();

        verify(shard0).shutdown();
        verify(shard1).shutdown();
    }

    @Test
    void testLifecycle_Initialize() throws Exception {
        CommitLog shard0 = createMockCommitLog(0);
        CommitLog shard1 = createMockCommitLog(1);
        manager.addCommitLog(0, shard0);
        manager.addCommitLog(1, shard1);

        manager.initialize();

        verify(shard0).initialize();
        verify(shard1).initialize();
    }

    @Test
    void testLifecycle_FullCycle() throws Exception {
        CommitLog shard0 = createMockCommitLog(0);
        manager.addCommitLog(0, shard0);

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
        return mock(CommitLog.class);
    }
}
