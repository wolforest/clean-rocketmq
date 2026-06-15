package cn.coderule.wolfmq.store.api;

import cn.coderule.wolfmq.domain.domain.meta.order.OrderRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeOrderStoreImplTest {

    private ConsumeOrderService orderService;
    private ConsumeOrderStoreImpl store;

    @BeforeEach
    void setUp() {
        orderService = mock(ConsumeOrderService.class);
        store = new ConsumeOrderStoreImpl(orderService);
    }

    @Test
    void isLocked_ShouldDelegateToService() {
        when(orderService.isLocked(any())).thenReturn(true);
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();

        assertTrue(store.isLocked(request));
        verify(orderService).isLocked(request);
    }

    @Test
    void lock_ShouldDelegateToService() {
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();

        store.lock(request);

        verify(orderService).lock(request);
    }

    @Test
    void unlock_ShouldDelegateToService() {
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();

        store.unlock(request);

        verify(orderService).unlock(request);
    }

    @Test
    void commit_ShouldDelegateToService() {
        when(orderService.commit(any())).thenReturn(42L);
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();

        long result = store.commit(request);

        assertEquals(42L, result);
        verify(orderService).commit(request);
    }
}