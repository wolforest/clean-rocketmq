package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.domain.store.api.meta.AckStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedAckStoreTest {

    private EmbedAckStore store;
    private AckStore ackStore;
    private EmbedLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        ackStore = mock(AckStore.class);
        loadBalance = mock(EmbedLoadBalance.class);
        store = new EmbedAckStore(ackStore, loadBalance);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }

    @Test
    void containsTopic_ShouldDelegateToLoadBalance() {
        when(loadBalance.containsTopic("test-topic")).thenReturn(true);
        assertTrue(store.containsTopic("test-topic"));
    }
}
