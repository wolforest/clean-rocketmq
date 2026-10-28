package com.wolfmq.store.server.ha;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.server.bootstrap.StoreContext;
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

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void initialize_ShouldRegisterCommitLogSynchronizer(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        StoreRegister storeRegister = mock(StoreRegister.class);
        StoreContext.register(storeConfig);
        StoreContext.register(storeRegister, StoreRegister.class);

        HABootstrap bootstrap = new HABootstrap();
        bootstrap.initialize();

        CommitLogSynchronizer synchronizer = StoreContext.getBean(CommitLogSynchronizer.class);
        assertNotNull(synchronizer);
    }
}