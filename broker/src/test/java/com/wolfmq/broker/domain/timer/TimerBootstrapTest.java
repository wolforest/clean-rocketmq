package com.wolfmq.broker.domain.timer;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final TimerBootstrap bootstrap = new TimerBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}