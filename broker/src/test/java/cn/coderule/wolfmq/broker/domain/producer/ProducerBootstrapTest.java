package cn.coderule.wolfmq.broker.domain.producer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProducerBootstrapTest {

    private final ProducerBootstrap bootstrap = new ProducerBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}