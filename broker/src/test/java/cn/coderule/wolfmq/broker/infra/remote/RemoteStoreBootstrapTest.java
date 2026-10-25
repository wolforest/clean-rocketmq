package cn.coderule.wolfmq.broker.infra.remote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoteStoreBootstrapTest {

    @Test
    void testRemoteStoreBootstrap_constructor() {
        RemoteStoreBootstrap bootstrap = new RemoteStoreBootstrap();
        assertNotNull(bootstrap);
    }
}
