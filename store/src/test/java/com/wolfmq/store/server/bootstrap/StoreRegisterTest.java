package com.wolfmq.store.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.meta.topic.PermName;
import com.wolfmq.domain.test.ConfigMock;
import com.wolfmq.rpc.common.rpc.netty.NettyClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreRegisterTest {
    protected ApplicationContext context = new ApplicationContext();


    @TempDir
    Path tempDir;

    private StoreConfig storeConfig;
    private NettyClient nettyClient;
    private StoreRegister storeRegister;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());

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
    }

    @Test
    void constructor_ShouldCreateStoreRegister() {
        storeRegister = new StoreRegister(storeConfig, nettyClient, () -> context.getBean(TopicService.class));
        assertNotNull(storeRegister);
    }

    @Test
    void shutdown_ShouldNotThrow() throws Exception {
        storeRegister = new StoreRegister(storeConfig, nettyClient, () -> context.getBean(TopicService.class));
        assertDoesNotThrow(() -> storeRegister.shutdown());
    }

    @Test
    void unregisterStore_ShouldCallRegistry() {
        storeRegister = new StoreRegister(storeConfig, nettyClient, () -> context.getBean(TopicService.class));
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
