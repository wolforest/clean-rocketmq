package cn.coderule.wolfmq.store.server.bootstrap;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComponentRegisterTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void execute_ShouldRegisterAllComponents() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        StoreContext.register(storeConfig);
        StoreContext.register(mock(NettyClient.class));

        StoreContext.CHECK_POINT = new StoreCheckpoint(tempDir.toString());

        LifecycleManager manager = ComponentRegister.register();
        assertNotNull(manager);
    }

    @Test
    void execute_ShouldRegisterStoreConfig() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        StoreContext.register(storeConfig);
        StoreContext.register(mock(NettyClient.class));

        StoreContext.CHECK_POINT = new StoreCheckpoint(tempDir.toString());

        ComponentRegister.register();

        StoreConfig retrievedConfig = StoreContext.getBean(StoreConfig.class);
        assertNotNull(retrievedConfig);
        assertEquals(storeConfig, retrievedConfig);
    }

    @Test
    void execute_ShouldRegisterScheduler() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        StoreContext.register(storeConfig);
        StoreContext.register(mock(NettyClient.class));

        StoreContext.CHECK_POINT = new StoreCheckpoint(tempDir.toString());

        ComponentRegister.register();

        assertNotNull(StoreContext.getScheduler());
    }
}
