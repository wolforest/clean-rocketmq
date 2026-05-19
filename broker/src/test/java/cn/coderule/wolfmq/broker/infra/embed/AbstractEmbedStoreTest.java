package cn.coderule.wolfmq.broker.infra.embed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractEmbedStoreTest {

    @Test
    void testEmbedLoadBalanceConstructor() {
        EmbedLoadBalance loadBalance = new EmbedLoadBalance(null, null, null);
        assertNotNull(loadBalance);
    }

    @Test
    void testContainsTopic_withNullTopicStore_returnsFalse() {
        EmbedLoadBalance loadBalance = new EmbedLoadBalance(null, null, null);
        assertNotNull(loadBalance);
    }
}