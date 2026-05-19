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

class EmbedLoadBalanceTest {

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
    void testContainsTopic_whenEmbedDisabled_returnsFalse() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(false);

        boolean result = loadBalance.containsTopic("testTopic");

        assertFalse(result);
    }

    @Test
    void testContainsTopic_whenEmbedEnabledAndTopicExists_returnsTrue() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(true);
        when(topicStore.exists("testTopic")).thenReturn(true);

        boolean result = loadBalance.containsTopic("testTopic");

        assertTrue(result);
    }

    @Test
    void testContainsTopic_whenEmbedEnabledAndTopicNotExistsAndNotSystemTopic_returnsFalse() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(true);
        when(topicStore.exists("testTopic")).thenReturn(false);

        boolean result = loadBalance.containsTopic("testTopic");

        assertFalse(result);
    }

    @Test
    void testContainsSubscription_whenEmbedDisabled_returnsFalse() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(false);

        boolean result = loadBalance.containsSubscription("testGroup");

        assertFalse(result);
    }

    @Test
    void testContainsSubscription_whenEmbedEnabledAndGroupExists_returnsTrue() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(true);
        when(subscriptionStore.existsGroup("testGroup")).thenReturn(true);

        boolean result = loadBalance.containsSubscription("testGroup");

        assertTrue(result);
    }

    @Test
    void testIsEmbed_whenStoreGroupEqualsBrokerGroup_returnsTrue() {
        when(brokerConfig.getGroup()).thenReturn("brokerGroup");

        boolean result = loadBalance.isEmbed("brokerGroup");

        assertTrue(result);
    }

    @Test
    void testIsEmbed_whenStoreGroupNotEqualsBrokerGroup_returnsFalse() {
        when(brokerConfig.getGroup()).thenReturn("brokerGroup");

        boolean result = loadBalance.isEmbed("otherGroup");

        assertFalse(result);
    }
}