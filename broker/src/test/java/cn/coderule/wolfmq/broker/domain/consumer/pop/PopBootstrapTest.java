package cn.coderule.wolfmq.broker.domain.consumer.pop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PopBootstrapTest {

    private final PopBootstrap bootstrap = new PopBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}