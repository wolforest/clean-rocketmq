package cn.coderule.wolfmq.broker.infra;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrokerRegisterTest {

    private BrokerRegister brokerRegister;
    private BrokerConfig brokerConfig;
    private NettyClient nettyClient;

    @BeforeEach
    void setUp() {
        brokerConfig = ConfigMock.createBrokerConfig();
        nettyClient = mock(NettyClient.class);
        brokerRegister = new BrokerRegister(brokerConfig, nettyClient);
    }

    @Test
    void constructor_ShouldCreateBrokerRegister() {
        assertNotNull(brokerRegister);
    }

    @Test
    void getRegistryClient_ShouldReturnNonNull() {
        assertNotNull(brokerRegister.getRegistryClient());
    }

    @Test
    void shutdown_ShouldNotThrow() {
        assertDoesNotThrow(() -> brokerRegister.shutdown());
    }
}
