package cn.coderule.wolfmq.broker.domain.consumer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerBootstrapTest {

    private final ConsumerBootstrap bootstrap = new ConsumerBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}