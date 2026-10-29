package com.wolfmq.broker.domain.transaction;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final TransactionBootstrap bootstrap = new TransactionBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}