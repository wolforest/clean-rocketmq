package cn.coderule.wolfmq.store.server.ha.server;

import cn.coderule.wolfmq.store.server.ha.core.HAConnection;
import cn.coderule.wolfmq.store.server.ha.core.HAContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HAServerTest {

    @Test
    void testInterfaceExists() {
        HAServer server = new TestHAServer();
        assertNotNull(server);
    }

    @Test
    void testGetConnectionList() {
        HAServer server = new TestHAServer();
        assertNotNull(server.getConnectionList());
        assertTrue(server.getConnectionList().isEmpty());
    }

    @Test
    void testGetHaContext() {
        HAServer server = new TestHAServer();
        assertNull(server.getHaContext());
    }

    @Test
    void testStartDoesNotThrow() {
        HAServer server = new TestHAServer();
        assertDoesNotThrow(server::start);
    }

    @Test
    void testShutdownDoesNotThrow() {
        HAServer server = new TestHAServer();
        assertDoesNotThrow(server::shutdown);
    }

    private static class TestHAServer implements HAServer {
        @Override
        public List<HAConnection> getConnectionList() {
            return List.of();
        }

        @Override
        public HAContext getHaContext() {
            return null;
        }

        @Override
        public void start() {}

        @Override
        public void shutdown() {}
    }
}
