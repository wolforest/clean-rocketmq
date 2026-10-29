package com.wolfmq.broker.domain.consumer;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final ConsumerBootstrap bootstrap = new ConsumerBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}