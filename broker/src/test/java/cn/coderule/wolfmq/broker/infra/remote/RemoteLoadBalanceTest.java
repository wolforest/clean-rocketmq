package cn.coderule.wolfmq.broker.infra.remote;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.rpc.registry.route.RouteLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteLoadBalanceTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private RouteLoader routeLoader;

    private RemoteLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loadBalance = new RemoteLoadBalance(brokerConfig, routeLoader);
    }

    @Test
    void testConstructor() {
        assertNotNull(loadBalance);
    }

    @Test
    void testFindByTopic_returnsNull() {
        String result = loadBalance.findByTopic("testTopic");
        assertNull(result);
    }

    @Test
    void testFindByGroup_returnsNull() {
        String result = loadBalance.findByGroup("testGroup", 1);
        assertNull(result);
    }

    @Test
    void testFindByStoreGroup_returnsNull() {
        String result = loadBalance.findByStoreGroup("storeGroup");
        assertNull(result);
    }
}