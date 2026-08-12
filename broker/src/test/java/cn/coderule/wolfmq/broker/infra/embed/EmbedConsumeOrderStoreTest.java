package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.domain.store.api.meta.ConsumeOrderStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedConsumeOrderStoreTest {

    private EmbedConsumeOrderStore store;
    private ConsumeOrderStore consumeOrderStore;
    private EmbedLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        consumeOrderStore = mock(ConsumeOrderStore.class);
        loadBalance = mock(EmbedLoadBalance.class);
        store = new EmbedConsumeOrderStore(consumeOrderStore, loadBalance);
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
