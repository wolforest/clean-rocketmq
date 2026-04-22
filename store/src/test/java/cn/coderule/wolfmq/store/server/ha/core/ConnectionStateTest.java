package cn.coderule.wolfmq.store.server.ha.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionStateTest {

    @Test
    void testEnumValues() {
        ConnectionState[] states = ConnectionState.values();
        assertEquals(5, states.length);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(ConnectionState.READY, ConnectionState.valueOf("READY"));
        assertEquals(ConnectionState.HANDSHAKE, ConnectionState.valueOf("HANDSHAKE"));
        assertEquals(ConnectionState.TRANSFER, ConnectionState.valueOf("TRANSFER"));
        assertEquals(ConnectionState.SUSPEND, ConnectionState.valueOf("SUSPEND"));
        assertEquals(ConnectionState.SHUTDOWN, ConnectionState.valueOf("SHUTDOWN"));
    }

    @Test
    void testReadyState() {
        assertEquals("READY", ConnectionState.READY.name());
    }

    @Test
    void testHandshakeState() {
        assertEquals("HANDSHAKE", ConnectionState.HANDSHAKE.name());
    }

    @Test
    void testTransferState() {
        assertEquals("TRANSFER", ConnectionState.TRANSFER.name());
    }

    @Test
    void testSuspendState() {
        assertEquals("SUSPEND", ConnectionState.SUSPEND.name());
    }

    @Test
    void testShutdownState() {
        assertEquals("SHUTDOWN", ConnectionState.SHUTDOWN.name());
    }
}