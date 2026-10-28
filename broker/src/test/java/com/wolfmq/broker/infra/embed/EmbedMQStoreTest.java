package com.wolfmq.broker.infra.embed;

import com.wolfmq.domain.server.store.api.MQStore;
import com.wolfmq.domain.domain.mq.dto.EnqueueRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedMQStoreTest {

    private EmbedMQStore store;
    private MQStore mqStore;
    private EmbedLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        mqStore = mock(MQStore.class);
        loadBalance = mock(EmbedLoadBalance.class);
        store = new EmbedMQStore(mqStore, loadBalance);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }

    @Test
    void enqueue_ShouldDelegate() {
        EnqueueRequest request = mock(EnqueueRequest.class);
        EnqueueResult result = mock(EnqueueResult.class);
        when(mqStore.enqueue(request)).thenReturn(result);

        EnqueueResult response = store.enqueue(request);
        assertEquals(result, response);
    }

    @Test
    void containsTopic_ShouldDelegateToLoadBalance() {
        when(loadBalance.containsTopic("test-topic")).thenReturn(true);
        assertTrue(store.containsTopic("test-topic"));
    }
}
