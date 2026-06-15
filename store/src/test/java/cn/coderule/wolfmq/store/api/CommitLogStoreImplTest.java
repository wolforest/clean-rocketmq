package cn.coderule.wolfmq.store.api;

import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitLogStoreImplTest {

    private CommitLogManager commitLogManager;
    private CommitLogStoreImpl store;

    @BeforeEach
    void setUp() {
        commitLogManager = mock(CommitLogManager.class);
        store = new CommitLogStoreImpl(commitLogManager);
    }

    @Test
    void select_ShouldDelegateToManager() {
        SelectedMappedBuffer expected = mock(SelectedMappedBuffer.class);
        when(commitLogManager.selectBuffer(100L)).thenReturn(expected);

        SelectedMappedBuffer result = store.select(100L);

        assertEquals(expected, result);
        verify(commitLogManager).selectBuffer(100L);
    }

    @Test
    void insert_ShouldDelegateToManager() {
        InsertResult expected = mock(InsertResult.class);
        byte[] data = new byte[10];
        when(commitLogManager.insert(100L, data, 0, 10)).thenReturn(expected);

        InsertResult result = store.insert(100L, data, 0, 10);

        assertEquals(expected, result);
        verify(commitLogManager).insert(100L, data, 0, 10);
    }

    @Test
    void getMinOffset_ShouldDelegateToManager() {
        when(commitLogManager.getMinOffset(0)).thenReturn(0L);

        assertEquals(0L, store.getMinOffset(0));
        verify(commitLogManager).getMinOffset(0);
    }

    @Test
    void getMaxOffset_ShouldDelegateToManager() {
        when(commitLogManager.getMaxOffset(0)).thenReturn(5000L);

        assertEquals(5000L, store.getMaxOffset(0));
        verify(commitLogManager).getMaxOffset(0);
    }

    @Test
    void getFlushedOffset_ShouldDelegateToManager() {
        when(commitLogManager.getFlushedOffset(0)).thenReturn(4000L);

        assertEquals(4000L, store.getFlushedOffset(0));
        verify(commitLogManager).getFlushedOffset(0);
    }

    @Test
    void getUnFlushedSize_ShouldDelegateToManager() {
        when(commitLogManager.getUnFlushedSize(0)).thenReturn(1000L);

        assertEquals(1000L, store.getUnFlushedSize(0));
        verify(commitLogManager).getUnFlushedSize(0);
    }
}