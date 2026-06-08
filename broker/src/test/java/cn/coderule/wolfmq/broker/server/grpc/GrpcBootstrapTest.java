package cn.coderule.wolfmq.broker.server.grpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcBootstrapTest {

    private final GrpcBootstrap bootstrap = new GrpcBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}