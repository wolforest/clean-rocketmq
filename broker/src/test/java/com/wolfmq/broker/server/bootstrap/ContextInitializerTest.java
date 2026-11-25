package com.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.container.EventBus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ContextInitializerTest {

    private final ApplicationContext context = new ApplicationContext();

    @Test
    void testConstructor() {
        String[] args = {"-p", "config"};
        ContextInitializer initializer = new ContextInitializer(args, context);

        assertNotNull(initializer);
    }

    @Test
    void testInitialize(@TempDir Path tmpDir) throws IOException {
        createBrokerConfig(tmpDir);
        String[] args = {"-p", tmpDir.toString()};
        ContextInitializer initializer = new ContextInitializer(args, context);

        assertDoesNotThrow(initializer::initialize);

        assertNotNull(context.getBean(com.wolfmq.common.config.server.BrokerConfig.class));
    }

    @Test
    void testStaticInit(@TempDir Path tmpDir) throws IOException {
        createBrokerConfig(tmpDir);
        String[] args = {"-p", tmpDir.toString()};

        assertDoesNotThrow(() -> ContextInitializer.init(args, context));
    }

    @Test
    void testInitLibsRegistersEventBus(@TempDir Path tmpDir) throws IOException {
        createBrokerConfig(tmpDir);
        String[] args = {"-p", tmpDir.toString()};
        ContextInitializer initializer = new ContextInitializer(args, context);
        initializer.initialize();

        EventBus eventBus = context.getBean(EventBus.class);
        assertNotNull(eventBus);
    }

    private void createBrokerConfig(Path dir) throws IOException {
        Files.writeString(dir.resolve("broker.yml"), "port: 8081\n");
    }
}
