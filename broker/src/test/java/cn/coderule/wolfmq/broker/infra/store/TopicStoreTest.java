package cn.coderule.wolfmq.broker.infra.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicStoreTest {

    @Test
    void testStoreBootstrap_constructor() {
        StoreBootstrap bootstrap = new StoreBootstrap();
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