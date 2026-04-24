package cn.coderule.wolfmq.store.server.ha.core;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.store.server.bootstrap.StoreRegister;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClusterServiceTest {

    @Test
    void testConstructorDoesNotThrow() {
        StoreConfig storeConfig = new StoreConfig();
        StoreRegister storeRegister = mock(StoreRegister.class);
        assertDoesNotThrow(() -> new ClusterService(storeConfig, storeRegister));
    }

    @Test
    void testStartDoesNotThrow() {
        StoreConfig storeConfig = new StoreConfig();
        StoreRegister storeRegister = mock(StoreRegister.class);
        ClusterService service = new ClusterService(storeConfig, storeRegister);
        assertDoesNotThrow(service::start);
    }

    @Test
    void testShutdownAfterStartDoesNotThrow() throws Exception {
        StoreConfig storeConfig = new StoreConfig();
        StoreRegister storeRegister = mock(StoreRegister.class);
        ClusterService service = new ClusterService(storeConfig, storeRegister);
        service.start();
        assertDoesNotThrow(service::shutdown);
    }
}
