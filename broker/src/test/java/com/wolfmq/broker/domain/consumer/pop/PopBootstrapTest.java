package com.wolfmq.broker.domain.consumer.pop;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PopBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final PopBootstrap bootstrap = new PopBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}