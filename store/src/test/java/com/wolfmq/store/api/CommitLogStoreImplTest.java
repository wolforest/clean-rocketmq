package com.wolfmq.store.api;

import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.domain.core.mmap.SelectedMappedBuffer;
import com.wolfmq.store.domain.commitlog.log.CommitLogProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitLogStoreImplTest {

    private CommitLogProxy commitLogProxy;
    private CommitLogStoreImpl store;

    @BeforeEach
    void setUp() {
        commitLogProxy = mock(CommitLogProxy.class);
        store = new CommitLogStoreImpl(commitLogProxy);
    }

    @Test
    void select_ShouldDelegateToManager() {
        SelectedMappedBuffer expected = mock(SelectedMappedBuffer.class);
        when(commitLogProxy.selectBuffer(100L)).thenReturn(expected);

        SelectedMappedBuffer result = store.select(100L);

        assertEquals(expected, result);
        verify(commitLogProxy).selectBuffer(100L);
    }

    @Test
    void insert_ShouldDelegateToManager() {
        InsertResult expected = mock(InsertResult.class);
        byte[] data = new byte[10];
        when(commitLogProxy.insert(100L, data, 0, 10)).thenReturn(expected);

        InsertResult result = store.insert(100L, data, 0, 10);

        assertEquals(expected, result);
        verify(commitLogProxy).insert(100L, data, 0, 10);
    }

    @Test
    void getMinOffset_ShouldDelegateToManager() {
        when(commitLogProxy.getMinOffset(0)).thenReturn(0L);

        assertEquals(0L, store.getMinOffset(0));
        verify(commitLogProxy).getMinOffset(0);
    }

    @Test
    void getMaxOffset_ShouldDelegateToManager() {
        when(commitLogProxy.getMaxOffset(0)).thenReturn(5000L);

        assertEquals(5000L, store.getMaxOffset(0));
        verify(commitLogProxy).getMaxOffset(0);
    }

    @Test
    void getFlushedOffset_ShouldDelegateToManager() {
        when(commitLogProxy.getFlushedOffset(0)).thenReturn(4000L);

        assertEquals(4000L, store.getFlushedOffset(0));
        verify(commitLogProxy).getFlushedOffset(0);
    }

    @Test
    void getUnFlushedSize_ShouldDelegateToManager() {
        when(commitLogProxy.getUnFlushedSize(0)).thenReturn(1000L);

        assertEquals(1000L, store.getUnFlushedSize(0));
        verify(commitLogProxy).getUnFlushedSize(0);
    }
}
