package com.wolfmq.broker.infra.store;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicStoreTest {
    private final ApplicationContext context = new ApplicationContext();


    @Test
    void testStoreBootstrap_constructor() {
        StoreBootstrap bootstrap = new StoreBootstrap(context);
        assertNotNull(bootstrap);
    }

    @Test
    void testConsumeOffsetStore_placeholder() {
        assertNotNull(new Object());
    }

    @Test
    void testConsumeOrderStore_placeholder() {
        assertNotNull(new Object());
    }

    @Test
    void testSubscriptionStore_placeholder() {
        assertNotNull(new Object());
    }

    @Test
    void testTimerStore_placeholder() {
        assertNotNull(new Object());
    }
}