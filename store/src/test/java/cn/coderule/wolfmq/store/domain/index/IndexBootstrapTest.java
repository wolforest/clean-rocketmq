package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.IndexConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.domain.store.domain.index.IndexService;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class IndexBootstrapTest {

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
    void initialize_ShouldRegisterIndexServiceAndHandler(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());
        storeConfig.setIndexConfig(new IndexConfig());

        StoreContext.register(storeConfig);
        CommitHandlerManager handlerManager = new CommitHandlerManager();
        StoreContext.register(handlerManager);

        IndexBootstrap bootstrap = new IndexBootstrap();
        bootstrap.initialize();

        DefaultIndexService service = StoreContext.getBean(DefaultIndexService.class);
        assertNotNull(service);

        assertNotNull(handlerManager);
    }

    @Test
    void shutdown_ShouldNotThrowWhenServiceNotInitialized() throws Exception {
        IndexBootstrap bootstrap = new IndexBootstrap();
        assertDoesNotThrow(() -> bootstrap.shutdown());
    }
}