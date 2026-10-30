package com.wolfmq.store.server.bootstrap;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.test.ConfigMock;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ContextInitializerTest {
    protected ApplicationContext context = new ApplicationContext();

    @AfterEach
    void tearDown() {
    }

    @Test
    void init_ShouldRegisterStoreConfig(@TempDir Path anotherDir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StoreArgument argument = StoreArgument.builder()
            .storeConfig(storeConfig)
            .build();

        ContextInitializer.init(argument, context);

        StoreConfig registered = context.getBean(StoreConfig.class);
        assertNotNull(registered);
    }
}
