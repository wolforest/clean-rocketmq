package com.wolfmq.store.domain.index;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.IndexConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class IndexBootstrapTest {
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
    void initialize_ShouldRegisterIndexServiceAndHandler(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());
        storeConfig.setIndexConfig(new IndexConfig());

        context.register(storeConfig);
        CommitHandlerManager handlerManager = new CommitHandlerManager();
        context.register(handlerManager);

        IndexBootstrap bootstrap = new IndexBootstrap(context);
        bootstrap.initialize();

        DefaultIndexService service = context.getBean(DefaultIndexService.class);
        assertNotNull(service);

        assertNotNull(handlerManager);
    }

    @Test
    void shutdown_ShouldNotThrowWhenServiceNotInitialized() throws Exception {
        IndexBootstrap bootstrap = new IndexBootstrap(context);
        assertDoesNotThrow(() -> bootstrap.shutdown());
    }
}
