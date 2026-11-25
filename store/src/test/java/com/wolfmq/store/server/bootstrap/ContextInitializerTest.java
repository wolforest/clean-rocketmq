package com.wolfmq.store.server.bootstrap;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.cli.StoreArgument;
import com.wolfmq.common.config.cli.StoreConfigLoader;
import com.wolfmq.common.config.server.StoreConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ContextInitializerTest {
    protected ApplicationContext context = new ApplicationContext();

    @Test
    void init_ShouldRegisterStoreConfig(@TempDir Path tmpDir) throws IOException {
        Path configFile = tmpDir.resolve("store.yml");
        Files.writeString(configFile, "port: 9000\n");

        StoreArgument argument = new StoreArgument();
        argument.setStoreConfig(configFile.toString());

        StoreConfigLoader configLoader = new StoreConfigLoader(argument);
        ContextInitializer.init(configLoader, context);

        StoreConfig registered = context.getBean(StoreConfig.class);
        assertNotNull(registered);
        assertEquals(9000, registered.getPort());
    }
}
