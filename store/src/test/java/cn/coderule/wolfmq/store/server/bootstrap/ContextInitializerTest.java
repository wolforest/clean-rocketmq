package cn.coderule.wolfmq.store.server.bootstrap;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ContextInitializerTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void init_ShouldRegisterStoreConfig(@TempDir Path anotherDir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StoreArgument argument = StoreArgument.builder()
            .storeConfig(storeConfig)
            .build();

        ContextInitializer.init(argument);

        StoreConfig registered = StoreContext.getBean(StoreConfig.class);
        assertNotNull(registered);
    }
}