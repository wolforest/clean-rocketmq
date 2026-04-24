package cn.coderule.wolfmq.store.server.ha.core;

import cn.coderule.wolfmq.store.server.ha.server.ConnectionContext;
import org.junit.jupiter.api.Test;

import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.*;

public class HAConnectionTest {

    @Test
    void testInterfaceExists() {
        HAConnection connection = new TestHAConnection();
        assertNotNull(connection);
    }

    @Test
    void testGetContext() {
        HAConnection connection = new TestHAConnection();
        assertNull(connection.getContext());
    }

    @Test
    void testGetSocketChannel() {
        HAConnection connection = new TestHAConnection();
        assertNull(connection.getSocketChannel());
    }

    @Test
    void testGetClientAddress() {
        HAConnection connection = new TestHAConnection();
        assertNull(connection.getClientAddress());
    }

    @Test
    void testGetConnectionState() {
        HAConnection connection = new TestHAConnection();
        assertEquals(ConnectionState.TRANSFER, connection.getConnectionState());
    }

    @Test
    void testSetConnectionState() {
        HAConnection connection = new TestHAConnection();
        connection.setConnectionState(ConnectionState.READY);
        assertEquals(ConnectionState.READY, connection.getConnectionState());
    }

    @Test
    void testIsSlaveHealthy() {
        HAConnection connection = new TestHAConnection();
        assertTrue(connection.isSlaveHealthy(1000));
    }

    @Test
    void testGetSlaveOffset() {
        HAConnection connection = new TestHAConnection();
        assertEquals(0, connection.getSlaveOffset());
    }

    @Test
    void testCloseDoesNotThrow() {
        HAConnection connection = new TestHAConnection();
        assertDoesNotThrow(connection::close);
    }

    private static class TestHAConnection implements HAConnection {
        private ConnectionState state = ConnectionState.TRANSFER;

        @Override
        public ConnectionContext getContext() { return null; }

        @Override
        public SocketChannel getSocketChannel() { return null; }

        @Override
        public String getClientAddress() { return null; }

        @Override
        public java.nio.channels.Selector openSelector() { return null; }

        @Override
        public java.nio.channels.SelectionKey registerSelector(java.nio.channels.Selector selector, int ops) { return null; }

        @Override
        public java.nio.channels.SelectionKey keyFor(java.nio.channels.Selector selector) { return null; }

        @Override
        public ConnectionState getConnectionState() { return state; }

        @Override
        public void setConnectionState(ConnectionState state) { this.state = state; }

        @Override
        public boolean isSlaveHealthy(long masterOffset) { return true; }

        @Override
        public long getSlaveOffset() { return 0; }

        @Override
        public void start() {}

        @Override
        public void close() {}
    }
}
