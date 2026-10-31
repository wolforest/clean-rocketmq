package com.wolfmq.store.domain.commitlog;

import com.wolfmq.domain.server.store.CheckPoint;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.CommitLogStore;
import com.wolfmq.domain.test.ConfigMock;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CommitLogBootstrapTest {
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
    void initialize_ShouldRegisterCommitLogBeans(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        context.register(storeConfig);
        StoreCheckpoint __cp = new StoreCheckpoint(anotherDir.toString()); context.register(__cp, CheckPoint.class);
        context.register(mock(TopicService.class), TopicService.class);
        context.register(mock(AllocateMappedFileService.class));

        CommitLogBootstrap bootstrap = new CommitLogBootstrap(context);
        bootstrap.initialize();

        CommitLogProxy manager = context.getBean(CommitLogProxy.class);
        assertNotNull(manager);

        CommitLogStore store = context.getBean(CommitLogStore.class, ContextNamespace.API);
        assertNotNull(store);
    }
}
