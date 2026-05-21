package cn.coderule.wolfmq.broker.infra.remote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractRemoteStoreTest {

    @Test
    void testRemoteLoadBalanceConstructor() {
        RemoteLoadBalance loadBalance = new RemoteLoadBalance(null, null);
        assertNotNull(loadBalance);
    }

    @Test
    void testFindByTopic_returnsNull() {
        RemoteLoadBalance loadBalance = new RemoteLoadBalance(null, null);
        assertNull(loadBalance.findByTopic("testTopic"));
    }

    @Test
    void testFindByGroup_returnsNull() {
        RemoteLoadBalance loadBalance = new RemoteLoadBalance(null, null);
        assertNull(loadBalance.findByGroup("testGroup", 1));
    }

    @Test
    void testFindByStoreGroup_returnsNull() {
        RemoteLoadBalance loadBalance = new RemoteLoadBalance(null, null);
        assertNull(loadBalance.findByStoreGroup("storeGroup"));
    }
}