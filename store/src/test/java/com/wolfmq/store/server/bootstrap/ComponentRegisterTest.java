package com.wolfmq.store.server.bootstrap;

import com.wolfmq.store.infra.StoreScheduler;

import com.wolfmq.domain.server.store.CheckPoint;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.LifecycleManager;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.rpc.common.rpc.netty.NettyClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComponentRegisterTest {
    protected ApplicationContext context = new ApplicationContext();


    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void execute_ShouldRegisterAllComponents() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        context.register(storeConfig, ContextNamespace.APP);
        context.register(mock(NettyClient.class), ContextNamespace.APP);

        StoreCheckpoint __cp = new StoreCheckpoint(tempDir.toString()); context.register(__cp, CheckPoint.class, ContextNamespace.APP); context.register(__cp, StoreCheckpoint.class, ContextNamespace.APP);

        LifecycleManager manager = new ComponentRegister(context).register();
        assertNotNull(manager);
    }

    @Test
    void execute_ShouldRegisterStoreConfig() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        context.register(storeConfig, ContextNamespace.APP);
        context.register(mock(NettyClient.class), ContextNamespace.APP);

        StoreCheckpoint __cp = new StoreCheckpoint(tempDir.toString()); context.register(__cp, CheckPoint.class, ContextNamespace.APP); context.register(__cp, StoreCheckpoint.class, ContextNamespace.APP);

        new ComponentRegister(context).register();

        StoreConfig retrievedConfig = context.getBean(StoreConfig.class, ContextNamespace.APP);
        assertNotNull(retrievedConfig);
        assertEquals(storeConfig, retrievedConfig);
    }

    @Test
    void execute_ShouldRegisterScheduler() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        context.register(storeConfig, ContextNamespace.APP);
        context.register(mock(NettyClient.class), ContextNamespace.APP);

        StoreCheckpoint __cp = new StoreCheckpoint(tempDir.toString()); context.register(__cp, CheckPoint.class, ContextNamespace.APP); context.register(__cp, StoreCheckpoint.class, ContextNamespace.APP);

        new ComponentRegister(context).register();

        assertNotNull(context.getBean(StoreScheduler.class, ContextNamespace.APP));
    }
}
