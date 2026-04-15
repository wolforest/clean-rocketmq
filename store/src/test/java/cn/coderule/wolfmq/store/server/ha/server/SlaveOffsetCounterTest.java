package cn.coderule.wolfmq.store.server.ha.server;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SlaveOffsetCounterTest {

    @Mock
    private StoreConfig storeConfig;

    private SlaveOffsetCounter slaveOffsetCounter;
    private ConnectionPool connectionPool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        connectionPool = new ConnectionPool();
        slaveOffsetCounter = new SlaveOffsetCounter(storeConfig, connectionPool);
    }

    @Test
    void testConstructor() {
        assertNotNull(slaveOffsetCounter);
        assertEquals(0, slaveOffsetCounter.getMaxOffset());
    }

    @Test
    void testGetMaxOffset() {
        assertEquals(0, slaveOffsetCounter.getMaxOffset());
    }

    @Test
    void testIsSlaveOkWithNoConnections() {
        when(storeConfig.getMaxSlaveGap()).thenReturn(1000);
        
        assertFalse(slaveOffsetCounter.isSlaveOk(100));
    }

    @Test
    void testUpdate() {
        slaveOffsetCounter.update(100L);
        
        assertEquals(100, slaveOffsetCounter.getMaxOffset());
    }

    @Test
    void testSetMaxOffset() {
        slaveOffsetCounter.setMaxOffset(500L);
        
        assertEquals(500, slaveOffsetCounter.getMaxOffset());
    }

    @Test
    void testSetMaxOffsetTwice() {
        slaveOffsetCounter.setMaxOffset(500L);
        slaveOffsetCounter.setMaxOffset(300L);
        
        assertEquals(500, slaveOffsetCounter.getMaxOffset());
    }

    @Test
    void testSetMinOffset() {
        slaveOffsetCounter.setMinOffset(100L);
    }
}
