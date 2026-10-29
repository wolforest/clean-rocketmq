package com.wolfmq.store.server.bootstrap;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.mock.ConfigMock;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ContextInitializerTest {
    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getBeanMap().clear();
        StoreContext.API.getBeanMap().clear();
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
