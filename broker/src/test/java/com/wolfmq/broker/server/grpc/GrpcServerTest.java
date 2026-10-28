package com.wolfmq.broker.server.grpc;

import com.wolfmq.domain.config.network.GrpcConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrpcServerTest {

    @Test
    void testConstructorWithNullService() {
        GrpcConfig config = new GrpcConfig();
        GrpcServer server = new GrpcServer(config, null);
        assertNotNull(server);
}
}