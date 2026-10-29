package com.wolfmq.broker.domain.meta;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetaBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final MetaBootstrap bootstrap = new MetaBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}