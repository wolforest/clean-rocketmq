package com.wolfmq.broker.domain.consumer.revive;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviveBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final ReviveBootstrap bootstrap = new ReviveBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}