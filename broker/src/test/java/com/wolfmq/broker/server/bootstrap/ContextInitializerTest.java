package com.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.container.EventBus;
import java.io.IOException;
import org.junit.jupiter.api.Test;

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
    void testInitialize() throws IOException {
        String[] args = {"-b", "broker.yml"};
        ContextInitializer initializer = new ContextInitializer(args, context);

        assertDoesNotThrow(initializer::initialize);

        assertNotNull(context.getBean(com.wolfmq.common.config.server.BrokerConfig.class));
    }

    @Test
    void testStaticInit() {
        String[] args = {"-b", "broker.yml"};

        assertDoesNotThrow(() -> ContextInitializer.init(args, context));
    }

    @Test
    void testInitLibsRegistersEventBus() throws IOException {
        String[] args = {"-b", "broker.yml"};
        ContextInitializer initializer = new ContextInitializer(args, context);
        initializer.initialize();

        EventBus eventBus = context.getBean(EventBus.class);
        assertNotNull(eventBus);
    }
}
