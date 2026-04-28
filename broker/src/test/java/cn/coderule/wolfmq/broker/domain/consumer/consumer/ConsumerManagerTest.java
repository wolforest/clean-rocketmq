package cn.coderule.wolfmq.broker.domain.consumer.consumer;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ConsumerInfo;
import cn.coderule.wolfmq.domain.domain.consumer.running.ConsumerGroupInfo;
import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumerManagerTest {

    @Mock
    private Channel mockChannel;

    @Mock
    private BrokerConfig brokerConfig;

    private ConsumerManager consumerManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(brokerConfig.getChannelExpireTime()).thenReturn(120000L);
        when(brokerConfig.getSubscriptionExpireTime()).thenReturn(600000L);
        consumerManager = new ConsumerManager(brokerConfig);
    }

    @Test
    void testRegisterNewConsumer() {
        String groupName = "testGroup";
        String clientId = "client1";
        Set<SubscriptionData> subscriptions = new HashSet<>();

        ConsumerInfo consumerInfo = createConsumerInfo(groupName, clientId, subscriptions);

        boolean updated = consumerManager.register(consumerInfo);

        ConsumerGroupInfo groupInfo = consumerManager.getGroupInfo(groupName);
        assertNotNull(groupInfo);
        assertTrue(updated);
    }

    @Test
    void testRegisterSameConsumerTwice() {
        String groupName = "testGroup";
        String clientId = "client1";
        Set<SubscriptionData> subscriptions = new HashSet<>();

        ConsumerInfo consumerInfo = createConsumerInfo(groupName, clientId, subscriptions);

        consumerManager.register(consumerInfo);
        boolean updated = consumerManager.register(consumerInfo);

        assertFalse(updated);
    }

    @Test
    void testUnregisterConsumer() {
        String groupName = "testGroup";
        String clientId = "client1";
        Set<SubscriptionData> subscriptions = new HashSet<>();

        ConsumerInfo consumerInfo = createConsumerInfo(groupName, clientId, subscriptions);

        consumerManager.register(consumerInfo);
        consumerManager.unregister(consumerInfo);

        ConsumerGroupInfo groupInfo = consumerManager.getGroupInfo(groupName);
        assertNull(groupInfo);
    }

    @Test
    void testFindChannelByClientId() {
        String groupName = "testGroup";
        String clientId = "client1";
        Set<SubscriptionData> subscriptions = new HashSet<>();

        ConsumerInfo consumerInfo = createConsumerInfo(groupName, clientId, subscriptions);
        consumerManager.register(consumerInfo);

        ClientChannelInfo found = consumerManager.findChannel(groupName, clientId);
        assertNotNull(found);
        assertEquals(clientId, found.getClientId());
    }

    @Test
    void testFindChannelNotExists() {
        ClientChannelInfo found = consumerManager.findChannel("nonexistent", "client1");
        assertNull(found);
    }

    @Test
    void testCompensateSubscription() {
        String groupName = "testGroup";
        String topic = "TestTopic";
        SubscriptionData subscription = new SubscriptionData();
        subscription.setTopic(topic);

        consumerManager.compensateSubscription(groupName, topic, subscription);

        SubscriptionData found = consumerManager.findSubscription(groupName, topic, true);
        assertNotNull(found);
        assertEquals(topic, found.getTopic());
    }

    @Test
    void testFindSubscriptionFromMain() {
        String groupName = "testGroup";
        String clientId = "client1";
        String topic = "TestTopic";

        SubscriptionData subscription = new SubscriptionData();
        subscription.setTopic(topic);
        Set<SubscriptionData> subscriptions = new HashSet<>();
        subscriptions.add(subscription);

        ConsumerInfo consumerInfo = createConsumerInfo(groupName, clientId, subscriptions);
        consumerManager.register(consumerInfo);

        SubscriptionData found = consumerManager.findSubscription(groupName, topic);
        assertNotNull(found);
    }

    @Test
    void testFindSubscriptionNotExists() {
        SubscriptionData found = consumerManager.findSubscription("nonexistent", "nonexistent");
        assertNull(found);
    }

    private ConsumerInfo createConsumerInfo(String groupName, String clientId, Set<SubscriptionData> subscriptions) {
        ClientChannelInfo channelInfo = ClientChannelInfo.builder()
            .channel(mockChannel)
            .clientId(clientId)
            .lastUpdateTime(System.currentTimeMillis())
            .build();

        return ConsumerInfo.builder()
            .groupName(groupName)
            .channelInfo(channelInfo)
            .subscriptionSet(subscriptions)
            .enableNotification(false)
            .enableModification(true)
            .build();
    }
}
