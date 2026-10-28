package com.wolfmq.broker.infra.embed;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.meta.offset.OffsetRequest;
import com.wolfmq.domain.domain.meta.offset.OffsetResult;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.mock.ConfigMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedConsumeOffsetStoreTest {

    private EmbedConsumeOffsetStore store;
    private ConsumeOffsetStore consumeOffsetStore;
    private EmbedLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        consumeOffsetStore = mock(ConsumeOffsetStore.class);
        loadBalance = mock(EmbedLoadBalance.class);
        store = new EmbedConsumeOffsetStore(consumeOffsetStore, loadBalance);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }

    @Test
    void getOffset_ShouldDelegate() {
        OffsetRequest request = mock(OffsetRequest.class);
        OffsetResult result = OffsetResult.notFound();
        when(consumeOffsetStore.getOffset(request)).thenReturn(result);

        OffsetResult response = store.getOffset(request);
        assertEquals(result, response);
    }

    @Test
    void putOffset_ShouldDelegate() {
        OffsetRequest request = mock(OffsetRequest.class);
        store.putOffset(request);
        verify(consumeOffsetStore).putOffset(request);
    }

    @Test
    void containsTopic_ShouldDelegateToLoadBalance() {
        when(loadBalance.containsTopic("test-topic")).thenReturn(true);
        assertTrue(store.containsTopic("test-topic"));
    }

    @Test
    void containsSubscription_ShouldDelegateToLoadBalance() {
        when(loadBalance.containsSubscription("test-group")).thenReturn(true);
        assertTrue(store.containsSubscription("test-group"));
    }
}
