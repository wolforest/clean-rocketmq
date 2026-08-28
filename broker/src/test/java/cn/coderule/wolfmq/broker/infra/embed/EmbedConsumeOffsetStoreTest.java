package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.wolfmq.domain.domain.store.api.meta.ConsumeOffsetStore;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
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
