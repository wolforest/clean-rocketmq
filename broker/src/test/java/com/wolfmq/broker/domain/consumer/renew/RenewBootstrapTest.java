package com.wolfmq.broker.domain.consumer.renew;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenewBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final RenewBootstrap bootstrap = new RenewBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}