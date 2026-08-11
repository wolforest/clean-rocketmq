package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.domain.store.api.TimerStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedTimerStoreTest {

    private EmbedTimerStore store;
    private TimerStore timerStore;
    private EmbedLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        timerStore = mock(TimerStore.class);
        loadBalance = mock(EmbedLoadBalance.class);
        store = new EmbedTimerStore(timerStore, loadBalance);
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
