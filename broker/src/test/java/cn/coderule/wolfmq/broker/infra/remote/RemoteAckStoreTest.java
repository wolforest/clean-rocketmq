package cn.coderule.wolfmq.broker.infra.remote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteAckStoreTest {

    private RemoteAckStore store;
    private RemoteLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        loadBalance = mock(RemoteLoadBalance.class);
        store = new RemoteAckStore(loadBalance);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }

    @Test
    void getLatestOffset_ShouldReturnNegativeOne() {
        long result = store.getLatestOffset("test-topic", "test-group", 0);
        assertEquals(-1L, result);
    }
}
