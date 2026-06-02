package cn.coderule.wolfmq.broker.domain.consumer.renew;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenewBootstrapTest {

    private final RenewBootstrap bootstrap = new RenewBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}