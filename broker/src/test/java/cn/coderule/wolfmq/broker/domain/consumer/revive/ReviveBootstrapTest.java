package cn.coderule.wolfmq.broker.domain.consumer.revive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviveBootstrapTest {

    private final ReviveBootstrap bootstrap = new ReviveBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}