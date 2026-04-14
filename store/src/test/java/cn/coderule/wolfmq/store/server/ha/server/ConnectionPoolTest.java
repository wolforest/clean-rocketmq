package cn.coderule.wolfmq.store.server.ha.server;

import cn.coderule.wolfmq.store.server.ha.core.HAConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionPoolTest {

    @Mock
    private HAConnection mockConnection1;

    @Mock
    private HAConnection mockConnection2;

    private ConnectionPool connectionPool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        connectionPool = new ConnectionPool();
    }

    @Test
    void testConstructor() {
        assertNotNull(connectionPool);
        assertEquals(0, connectionPool.getConnectionCount());
    }

    @Test
    void testAddConnection() {
        connectionPool.addConnection(mockConnection1);

        assertEquals(1, connectionPool.getConnectionCount());
    }

    @Test
    void testAddMultipleConnections() {
        connectionPool.addConnection(mockConnection1);
        connectionPool.addConnection(mockConnection2);

        assertEquals(2, connectionPool.getConnectionCount());
    }

    @Test
    void testRemoveConnection() {
        connectionPool.addConnection(mockConnection1);
        connectionPool.addConnection(mockConnection2);
        connectionPool.removeConnection(mockConnection1);

        assertEquals(1, connectionPool.getConnectionCount());
    }

    @Test
    void testRemoveNonExistentConnection() {
        connectionPool.addConnection(mockConnection1);
        connectionPool.removeConnection(mockConnection2);

        assertEquals(0, connectionPool.getConnectionCount());
    }

    @Test
    void testCountHealthyConnection() {
        when(mockConnection1.isSlaveHealthy(anyLong())).thenReturn(true);
        when(mockConnection2.isSlaveHealthy(anyLong())).thenReturn(false);

        connectionPool.addConnection(mockConnection1);
        connectionPool.addConnection(mockConnection2);

        int count = connectionPool.countHealthyConnection(100L);
        assertEquals(2, count);
    }

    @Test
    void testCountHealthyConnectionWithNoConnections() {
        int count = connectionPool.countHealthyConnection(100L);
        assertEquals(1, count);
    }
}
