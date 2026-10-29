package com.wolfmq.registry.domain.broker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BrokerBootstrapTest {

    @Test
    void testLifecycleDoesNotThrow() {
        BrokerBootstrap manager = new BrokerBootstrap();
        assertDoesNotThrow(() -> manager.start());
        assertDoesNotThrow(() -> manager.shutdown());
    }
}
