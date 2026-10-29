package com.wolfmq.broker.domain.producer;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProducerBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final ProducerBootstrap bootstrap = new ProducerBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}