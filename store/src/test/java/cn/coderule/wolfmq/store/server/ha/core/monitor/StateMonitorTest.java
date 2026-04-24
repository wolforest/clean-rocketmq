package cn.coderule.wolfmq.store.server.ha.core.monitor;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.store.server.ha.client.HAClient;
import cn.coderule.wolfmq.store.server.ha.core.ConnectionState;
import cn.coderule.wolfmq.store.server.ha.core.HAConnection;
import cn.coderule.wolfmq.store.server.ha.server.HAServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StateMonitorTest {

    private StoreConfig storeConfig;
    private HAServer haServer;

    @BeforeEach
    void setUp() {
        storeConfig = new StoreConfig();
        haServer = mock(HAServer.class);
    }

    @Test
    void testGetServiceName() {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        assertEquals("StateMonitor", monitor.getServiceName());
    }

    @Test
    void testConstructorWithoutHAClient() {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        assertNotNull(monitor);
    }

    @Test
    void testConstructorWithHAClient() {
        HAClient haClient = mock(HAClient.class);
        StateMonitor monitor = new StateMonitor(storeConfig, haServer, haClient);
        assertNotNull(monitor);
    }

    @Test
    void testSetRequestCancelsPreviousRequest() {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);

        StateRequest firstRequest = new StateRequest(ConnectionState.READY, "192.168.1.1", false);
        monitor.setRequest(firstRequest);

        assertFalse(firstRequest.getRequestFuture().isDone());

        StateRequest secondRequest = new StateRequest(ConnectionState.READY, "192.168.1.2", false);
        monitor.setRequest(secondRequest);

        assertTrue(firstRequest.getRequestFuture().isCancelled());
        assertFalse(secondRequest.getRequestFuture().isDone());
    }

    @Test
    void testSetRequestWithDifferentStates() {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);

        StateRequest transferRequest = new StateRequest(ConnectionState.TRANSFER, "10.0.0.1", false);
        monitor.setRequest(transferRequest);
        assertFalse(transferRequest.getRequestFuture().isDone());

        StateRequest readyRequest = new StateRequest(ConnectionState.READY, "10.0.0.2", true);
        monitor.setRequest(readyRequest);
        assertTrue(transferRequest.getRequestFuture().isCancelled());
    }

    @Test
    void testCheckConnectionStateAndNotifyWithNullRequest() {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        HAConnection connection = mock(HAConnection.class);

        assertFalse(monitor.checkConnectionStateAndNotify(connection));
        assertFalse(monitor.checkConnectionStateAndNotify(null));
    }

    @Test
    void testCheckConnectionStateAndNotifyWithNullConnection() {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        StateRequest request = new StateRequest(ConnectionState.READY, "192.168.1.1", false);
        monitor.setRequest(request);

        assertFalse(monitor.checkConnectionStateAndNotify(null));
    }

    @Test
    void testCheckConnectionStateAndNotifyWithMatchingState() throws Exception {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        StateRequest request = new StateRequest(ConnectionState.READY, "10.0.0.1", false);
        monitor.setRequest(request);

        HAConnection connection = mock(HAConnection.class);
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(connection.getSocketChannel()).thenReturn(socketChannel);
        when(socketChannel.getRemoteAddress()).thenReturn(
            new InetSocketAddress("10.0.0.1", 9123));
        when(connection.getConnectionState()).thenReturn(ConnectionState.READY);

        boolean result = monitor.checkConnectionStateAndNotify(connection);
        assertTrue(result);
        assertTrue(request.getRequestFuture().isDone());
    }

    @Test
    void testCheckConnectionStateAndNotifyWithMismatchedAddress() throws Exception {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        StateRequest request = new StateRequest(ConnectionState.READY, "10.0.0.1", false);
        monitor.setRequest(request);

        HAConnection connection = mock(HAConnection.class);
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(connection.getSocketChannel()).thenReturn(socketChannel);
        when(socketChannel.getRemoteAddress()).thenReturn(
            new InetSocketAddress("10.0.0.2", 9123));
        when(connection.getConnectionState()).thenReturn(ConnectionState.READY);

        boolean result = monitor.checkConnectionStateAndNotify(connection);
        assertFalse(result);
    }

    @Test
    void testCheckConnectionStateAndNotifyWithShutdownNotify() throws Exception {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        StateRequest request = new StateRequest(ConnectionState.READY, "10.0.0.1", true);
        monitor.setRequest(request);

        HAConnection connection = mock(HAConnection.class);
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(connection.getSocketChannel()).thenReturn(socketChannel);
        when(socketChannel.getRemoteAddress()).thenReturn(
            new InetSocketAddress("10.0.0.1", 9123));
        when(connection.getConnectionState()).thenReturn(ConnectionState.SHUTDOWN);

        boolean result = monitor.checkConnectionStateAndNotify(connection);
        assertTrue(result);
        assertTrue(request.getRequestFuture().isDone());
    }

    @Test
    void testCheckConnectionStateAndNotifyWithNonMatchingStateAndNoNotifyShutdown() throws Exception {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        StateRequest request = new StateRequest(ConnectionState.READY, "10.0.0.1", false);
        monitor.setRequest(request);

        HAConnection connection = mock(HAConnection.class);
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(connection.getSocketChannel()).thenReturn(socketChannel);
        when(socketChannel.getRemoteAddress()).thenReturn(
            new InetSocketAddress("10.0.0.1", 9123));
        when(connection.getConnectionState()).thenReturn(ConnectionState.TRANSFER);

        boolean result = monitor.checkConnectionStateAndNotify(connection);
        assertTrue(result);
        assertFalse(request.getRequestFuture().isDone());
    }

    @Test
    void testCheckConnectionStateAndNotifyWithException() throws Exception {
        StateMonitor monitor = new StateMonitor(storeConfig, haServer);
        StateRequest request = new StateRequest(ConnectionState.READY, "10.0.0.1", false);
        monitor.setRequest(request);

        HAConnection connection = mock(HAConnection.class);
        when(connection.getSocketChannel()).thenThrow(new RuntimeException("test exception"));

        boolean result = monitor.checkConnectionStateAndNotify(connection);
        assertFalse(result);
    }
}