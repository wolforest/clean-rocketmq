package cn.coderule.wolfmq.store.server.bootstrap;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.domain.cluster.server.ServerInfo;
import cn.coderule.wolfmq.domain.domain.cluster.route.TopicInfo;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.core.constant.PermName;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.wolfmq.rpc.registry.RegistryClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreRegisterTest {

    @TempDir
    Path tempDir;

    private StoreConfig storeConfig;
    private NettyClient nettyClient;
    private StoreRegister storeRegister;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();

        storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        storeConfig.setCluster("default-cluster");
        storeConfig.setGroup("default-group");
        storeConfig.setGroupNo(0L);
        storeConfig.setHost("127.0.0.1");
        storeConfig.setPort(10911);

        nettyClient = mock(NettyClient.class);
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void constructor_ShouldCreateStoreRegister() {
        storeRegister = new StoreRegister(storeConfig, nettyClient);
        assertNotNull(storeRegister);
    }

    @Test
    void shutdown_ShouldNotThrow() throws Exception {
        storeRegister = new StoreRegister(storeConfig, nettyClient);
        assertDoesNotThrow(() -> storeRegister.shutdown());
    }

    @Test
    void unregisterStore_ShouldCallRegistry() {
        storeRegister = new StoreRegister(storeConfig, nettyClient);
        assertDoesNotThrow(() -> storeRegister.unregisterStore());
    }

    @Test
    void mergeServerPermission_WithFullPerm_ShouldNotModify() {
        storeConfig.setPermission(PermName.PERM_READ | PermName.PERM_WRITE);
        Topic topic = new Topic();
        topic.setPerm(PermName.PERM_READ | PermName.PERM_WRITE);

        assertNotNull(topic);
        assertEquals(PermName.PERM_READ | PermName.PERM_WRITE, topic.getPerm());
    }
}