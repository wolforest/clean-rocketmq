package com.wolfmq.store.domain.index;


import com.wolfmq.common.domain.offset.QueueProgress;
import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.IndexConfig;
import com.wolfmq.common.testing.ConfigMock;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import com.wolfmq.store.domain.index.file.FileIndexService;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class IndexBootstrapTest {
    protected ApplicationContext context = new ApplicationContext();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterIndexServiceAndHandler(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        storeConfig.setIndex(new IndexConfig());

        context.register(storeConfig);
        CommitHandlerManager handlerManager = new CommitHandlerManager(mock(QueueProgress.class));
        context.register(handlerManager);

        IndexBootstrap bootstrap = new IndexBootstrap(context);
        bootstrap.initialize();

        FileIndexService service = context.getBean(FileIndexService.class);
        assertNotNull(service);

        assertNotNull(handlerManager);
    }

    @Test
    void shutdown_ShouldNotThrowWhenServiceNotInitialized() {
        IndexBootstrap bootstrap = new IndexBootstrap(context);
        assertDoesNotThrow(bootstrap::shutdown);
    }
}
