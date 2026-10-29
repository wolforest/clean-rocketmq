package com.wolfmq.store.server.ha;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.server.bootstrap.StoreRegister;
import com.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HABootstrapTest {
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
    void initialize_ShouldRegisterCommitLogSynchronizer(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        StoreRegister storeRegister = mock(StoreRegister.class);
        context.register(storeConfig, ContextNamespace.APP);
        context.register(storeRegister, StoreRegister.class, ContextNamespace.APP);

        HABootstrap bootstrap = new HABootstrap(context);
        bootstrap.initialize();

        CommitLogSynchronizer synchronizer = context.getBean(CommitLogSynchronizer.class, ContextNamespace.APP);
        assertNotNull(synchronizer);
    }
}
