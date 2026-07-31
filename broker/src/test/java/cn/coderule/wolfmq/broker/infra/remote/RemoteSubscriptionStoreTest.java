package cn.coderule.wolfmq.broker.infra.remote;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteSubscriptionStoreTest {

    private RemoteSubscriptionStore store;
    private BrokerConfig brokerConfig;
    private RemoteLoadBalance loadBalance;
    private RpcClient rpcClient;

    @BeforeEach
    void setUp() {
        brokerConfig = ConfigMock.createBrokerConfig();
        loadBalance = mock(RemoteLoadBalance.class);
        rpcClient = mock(RpcClient.class);
        store = new RemoteSubscriptionStore(brokerConfig, loadBalance, rpcClient);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }

    @Test
    void existsGroup_ShouldReturnFalse() {
        assertFalse(store.existsGroup("test-topic", "test-group"));
    }

    @Test
    void getGroup_ShouldReturnNull() {
        assertNull(store.getGroup("test-topic", "test-group"));
    }
}
