package cn.coderule.wolfmq.rpc.registry.service;

import cn.coderule.wolfmq.domain.config.network.RpcClientConfig;
import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistryManagerTest {

    private RpcClientConfig config;
    private NettyClient nettyClient;

    @BeforeEach
    void setUp() {
        config = new RpcClientConfig();
        nettyClient = mock(NettyClient.class);
    }

    @Test
    void constructor_ShouldInitializeWithSingleAddress() {
        RegistryManager manager = new RegistryManager(config, "127.0.0.1:9876", nettyClient);
        assertNotNull(manager);
    }

    @Test
    void shutdown_ShouldNotThrow() {
        RegistryManager manager = new RegistryManager(config, "127.0.0.1:9876", nettyClient);
        assertDoesNotThrow(() -> manager.shutdown());
    }

    @Test
    void constructor_WithNullAddressConfig() {
        assertDoesNotThrow(() -> new RegistryManager(config, null, nettyClient));
    }
}