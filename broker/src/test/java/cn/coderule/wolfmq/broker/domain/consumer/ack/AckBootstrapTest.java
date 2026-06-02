package cn.coderule.wolfmq.broker.domain.consumer.ack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AckBootstrapTest {

    private final AckBootstrap bootstrap = new AckBootstrap();

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