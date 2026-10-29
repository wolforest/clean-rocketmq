package com.wolfmq.broker.server.grpc;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    private final GrpcBootstrap bootstrap = new GrpcBootstrap(context);

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}