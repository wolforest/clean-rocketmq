package cn.coderule.wolfmq.store.server.ha.core.monitor;

import cn.coderule.wolfmq.store.server.ha.core.ConnectionState;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class StateRequestTest {

    @Test
    void testConstructorAndGetters() {
        StateRequest request = new StateRequest(ConnectionState.READY, "192.168.1.1", true);

        assertEquals(ConnectionState.READY, request.getExpectState());
        assertEquals("192.168.1.1", request.getRemoteAddr());
        assertTrue(request.isNotifyWhenShutdown());
    }

    @Test
    void testRequestFutureNotNull() {
        StateRequest request = new StateRequest(ConnectionState.READY, "10.0.0.1", false);

        assertNotNull(request.getRequestFuture());
        assertFalse(request.getRequestFuture().isDone());
    }

    @Test
    void testRequestFutureCompletes() {
        StateRequest request = new StateRequest(ConnectionState.READY, "10.0.0.1", false);

        request.getRequestFuture().complete(true);
        assertTrue(request.getRequestFuture().isDone());
    }

    @Test
    void testNotifyWhenShutdownFalse() {
        StateRequest request = new StateRequest(ConnectionState.TRANSFER, "127.0.0.1", false);
        assertFalse(request.isNotifyWhenShutdown());
    }

    @Test
    void testNotifyWhenShutdownTrue() {
        StateRequest request = new StateRequest(ConnectionState.TRANSFER, "127.0.0.1", true);
        assertTrue(request.isNotifyWhenShutdown());
    }

    @Test
    void testDifferentConnectionStates() {
        StateRequest transferRequest = new StateRequest(ConnectionState.TRANSFER, "host1", false);
        assertEquals(ConnectionState.TRANSFER, transferRequest.getExpectState());

        StateRequest readyRequest = new StateRequest(ConnectionState.READY, "host2", true);
        assertEquals(ConnectionState.READY, readyRequest.getExpectState());

        StateRequest shutdownRequest = new StateRequest(ConnectionState.SHUTDOWN, "host3", true);
        assertEquals(ConnectionState.SHUTDOWN, shutdownRequest.getExpectState());
    }
}