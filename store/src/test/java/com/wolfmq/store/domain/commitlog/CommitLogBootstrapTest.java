package com.wolfmq.store.domain.commitlog;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.CommitLogStore;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreContext;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class CommitLogBootstrapTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
        StoreContext.ISOLATED = false;
        StoreContext.STATE_MACHINE_VERSION = 0L;
    }

    @Test
    void initialize_ShouldRegisterCommitLogBeans(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        StoreContext.register(storeConfig);
        StoreContext.CHECK_POINT = new StoreCheckpoint(anotherDir.toString());

        CommitLogBootstrap bootstrap = new CommitLogBootstrap();
        bootstrap.initialize();

        CommitLogManager manager = StoreContext.getBean(CommitLogManager.class);
        assertNotNull(manager);

        CommitLogStore store = StoreContext.getAPI(CommitLogStore.class);
        assertNotNull(store);
    }
}
