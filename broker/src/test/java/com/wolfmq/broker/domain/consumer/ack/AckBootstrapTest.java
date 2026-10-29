package com.wolfmq.broker.domain.consumer.ack;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AckBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final AckBootstrap bootstrap = new AckBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }

    @Test
    void testStartDoesNotThrow() {
        assertDoesNotThrow(() -> bootstrap.start());
    }

    @Test
    void testShutdownDoesNotThrow() {
        assertDoesNotThrow(() -> bootstrap.shutdown());
    }
}