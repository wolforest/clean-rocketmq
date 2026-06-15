package cn.coderule.wolfmq.broker.server.rpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpcBootstrapTest {

    private final RpcBootstrap bootstrap = new RpcBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }

    @Test
    void testStartDoesNotThrow() {
        assertDoesNotThrow(() -> bootstrap.start());
    }

    @Test
    void testShutdownDoesNotThrow() {
        assertDoesNotThrow(() -> bootstrap.shutdown());
    }
}