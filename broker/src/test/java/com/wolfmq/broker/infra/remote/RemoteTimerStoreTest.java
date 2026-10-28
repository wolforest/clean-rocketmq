package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteTimerStoreTest {

    private RemoteTimerStore store;
    private BrokerConfig brokerConfig;
    private RemoteLoadBalance loadBalance;
    private RpcClient rpcClient;

    @BeforeEach
    void setUp() {
        brokerConfig = ConfigMock.createBrokerConfig();
        loadBalance = mock(RemoteLoadBalance.class);
        rpcClient = mock(RpcClient.class);
        store = new RemoteTimerStore(brokerConfig, loadBalance, rpcClient);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }
}
