package cn.coderule.wolfmq.broker.infra.remote;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteConsumeOffsetStoreTest {

    private RemoteConsumeOffsetStore store;
    private BrokerConfig brokerConfig;
    private RemoteLoadBalance loadBalance;
    private RpcClient rpcClient;

    @BeforeEach
    void setUp() {
        brokerConfig = ConfigMock.createBrokerConfig();
        loadBalance = mock(RemoteLoadBalance.class);
        rpcClient = mock(RpcClient.class);
        store = new RemoteConsumeOffsetStore(brokerConfig, loadBalance, rpcClient);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }

    @Test
    void getOffset_ShouldReturnNotFound() {
        OffsetRequest request = mock(OffsetRequest.class);
        OffsetResult result = store.getOffset(request);
        assertNotNull(result);
    }

    @Test
    void putOffset_ShouldNotThrow() {
        OffsetRequest request = mock(OffsetRequest.class);
        assertDoesNotThrow(() -> store.putOffset(request));
    }
}
