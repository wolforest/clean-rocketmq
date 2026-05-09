package cn.coderule.wolfmq.registry.domain.broker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BrokerManagerTest {

    @Test
    void testLifecycleDoesNotThrow() {
        BrokerManager manager = new BrokerManager();
        assertDoesNotThrow(() -> manager.start());
        assertDoesNotThrow(() -> manager.shutdown());
    }
}