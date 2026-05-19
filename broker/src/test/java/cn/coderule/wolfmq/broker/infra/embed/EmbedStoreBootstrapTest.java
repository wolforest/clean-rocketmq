package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.store.api.meta.SubscriptionStore;
import cn.coderule.wolfmq.domain.domain.store.api.meta.TopicStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedStoreBootstrapTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private TopicStore topicStore;

    @Mock
    private SubscriptionStore subscriptionStore;

    private EmbedLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loadBalance = new EmbedLoadBalance(brokerConfig, topicStore, subscriptionStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(loadBalance);
    }

    @Test
    void testContainsTopic_whenEmbedDisabled() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(false);
        assertFalse(loadBalance.containsTopic("test"));
    }

    @Test
    void testContainsSubscription_whenEmbedDisabled() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(false);
        assertFalse(loadBalance.containsSubscription("test"));
    }

    @Test
    void testIsEmbed_whenGroupsEqual() {
        when(brokerConfig.getGroup()).thenReturn("group1");
        assertTrue(loadBalance.isEmbed("group1"));
    }

    @Test
    void testIsEmbed_whenGroupsNotEqual() {
        when(brokerConfig.getGroup()).thenReturn("group1");
        assertFalse(loadBalance.isEmbed("group2"));
    }
}