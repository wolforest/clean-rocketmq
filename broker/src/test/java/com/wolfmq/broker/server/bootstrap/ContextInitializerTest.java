package com.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.container.EventBus;
import com.wolfmq.domain.core.constant.ContextNamespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextInitializerTest {

    private final ApplicationContext context = new ApplicationContext();

    @Test
    void testConstructor() {
        String[] args = {"-c", "config.properties"};
        ContextInitializer initializer = new ContextInitializer(args, context);

        assertNotNull(initializer);
    }

    @Test
    void testInitialize() {
        String[] args = {};
        ContextInitializer initializer = new ContextInitializer(args, context);

        assertDoesNotThrow(initializer::initialize);

        assertNotNull(context.getBean(com.wolfmq.domain.config.server.BrokerConfig.class, ContextNamespace.APP));
    }

    @Test
    void testStaticInit() {
        String[] args = {};

        assertDoesNotThrow(() -> ContextInitializer.init(args, context));
    }

    @Test
    void testInitLibsRegistersEventBus() {
        String[] args = {};
        ContextInitializer initializer = new ContextInitializer(args, context);
        initializer.initialize();

        EventBus eventBus = context.getBean(EventBus.class, ContextNamespace.APP);
        assertNotNull(eventBus);
    }
}
