package cn.coderule.wolfmq.broker.server.bootstrap;

import cn.coderule.wolfmq.domain.core.event.ServerEventBus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextInitializerTest {

    @Test
    void testConstructor() {
        String[] args = {"-c", "config.properties"};
        ContextInitializer initializer = new ContextInitializer(args);
        
        assertNotNull(initializer);
    }

    @Test
    void testInitialize() {
        String[] args = {};
        ContextInitializer initializer = new ContextInitializer(args);
        
        // Initialize should not throw
        assertDoesNotThrow(() -> initializer.initialize());
        
        // Verify config was loaded
        assertNotNull(BrokerContext.getBean(cn.coderule.wolfmq.domain.config.server.BrokerConfig.class));
    }

    @Test
    void testStaticInit() {
        String[] args = {};
        
        // Static init should not throw
        assertDoesNotThrow(() -> ContextInitializer.init(args));
    }

    @Test
    void testInitLibsRegistersEventBus() {
        String[] args = {};
        ContextInitializer initializer = new ContextInitializer(args);
        initializer.initialize();
        
        // Verify ServerEventBus was registered
        ServerEventBus eventBus = BrokerContext.getBean(ServerEventBus.class);
        assertNotNull(eventBus);
    }
}
