package cn.coderule.wolfmq.store.server.ha.server;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.store.server.ha.core.HAConnection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultHAServerTest {

    @Test
    void testConstructorDoesNotThrow() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setHaPort(0);
        assertDoesNotThrow(() -> new DefaultHAServer(storeConfig));
    }

    @Test
    void testGetConnectionList() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setHaPort(0);
        DefaultHAServer server = new DefaultHAServer(storeConfig);
        List<HAConnection> connections = server.getConnectionList();
        assertNotNull(connections);
        assertTrue(connections.isEmpty());
    }

    @Test
    void testGetHaContext() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setHaPort(0);
        DefaultHAServer server = new DefaultHAServer(storeConfig);
        assertNotNull(server.getHaContext());
    }

    @Test
    void testShutdownAfterStartDoesNotThrow() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setHaPort(0);
        DefaultHAServer server = new DefaultHAServer(storeConfig);
        assertDoesNotThrow(() -> {
            server.start();
            server.shutdown();
        });
    }
}