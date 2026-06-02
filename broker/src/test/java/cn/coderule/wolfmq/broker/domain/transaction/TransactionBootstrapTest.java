package cn.coderule.wolfmq.broker.domain.transaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionBootstrapTest {

    private final TransactionBootstrap bootstrap = new TransactionBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}