package com.wolfmq.broker.infra.remote;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoteStoreBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    @Test
    void testRemoteStoreBootstrap_constructor() {
        RemoteStoreBootstrap bootstrap = new RemoteStoreBootstrap(context);
        assertNotNull(bootstrap);
    }
}
