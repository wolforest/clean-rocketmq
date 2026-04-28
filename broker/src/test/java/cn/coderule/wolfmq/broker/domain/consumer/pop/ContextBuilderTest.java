package cn.coderule.wolfmq.broker.domain.consumer.pop;

import cn.coderule.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.wolfmq.rpc.store.facade.TopicFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContextBuilderTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private ConsumerManager consumerManager;

    @Mock
    private TopicFacade topicFacade;

    @Mock
    private SubscriptionFacade subscriptionFacade;

    private ContextBuilder contextBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setPopRetryProbability(50);
        when(brokerConfig.getMessageConfig()).thenReturn(messageConfig);
        contextBuilder = new ContextBuilder(brokerConfig, consumerManager, topicFacade, subscriptionFacade);
    }

    @Test
    void testBuildValidContext() {
        String topicName = "TestTopic";
        String consumerGroup = "testGroup";
        String retryTopicName = "%RETRY%" + consumerGroup + "_" + topicName;

        PopRequest request = createPopRequest(topicName, consumerGroup);

        Topic topic = Topic.builder()
            .topicName(topicName)
            .readQueueNums(8)
            .writeQueueNums(8)
            .perm(6)
            .build();

        Topic retryTopic = Topic.builder()
            .topicName(retryTopicName)
            .readQueueNums(1)
            .writeQueueNums(1)
            .perm(6)
            .build();

        SubscriptionGroup subscriptionGroup = SubscriptionGroup.builder()
            .groupName(consumerGroup)
            .consumeEnable(true)
            .build();

        when(topicFacade.getTopic(anyString())).thenReturn(null);
        when(topicFacade.getTopic(topicName)).thenReturn(topic);
        when(topicFacade.getTopic(retryTopicName)).thenReturn(retryTopic);
        when(subscriptionFacade.getGroup(topicName, consumerGroup)).thenReturn(subscriptionGroup);

        PopContext context = contextBuilder.build(request);

        assertNotNull(context);
        assertEquals(topic, context.getTopic());
    }

    @Test
    void testBuildTopicNotExists() {
        String topicName = "NonExistentTopic";
        String consumerGroup = "testGroup";

        PopRequest request = createPopRequest(topicName, consumerGroup);

        when(topicFacade.getTopic(topicName)).thenReturn(null);

        assertThrows(InvalidRequestException.class, () -> contextBuilder.build(request));
    }

    @Test
    void testBuildTopicNotReadable() {
        String topicName = "TestTopic";
        String consumerGroup = "testGroup";

        PopRequest request = createPopRequest(topicName, consumerGroup);

        Topic topic = Topic.builder()
            .topicName(topicName)
            .perm(0)
            .build();

        when(topicFacade.getTopic(topicName)).thenReturn(topic);

        assertThrows(InvalidRequestException.class, () -> contextBuilder.build(request));
    }

    @Test
    void testBuildSubscriptionGroupNotExists() {
        String topicName = "TestTopic";
        String consumerGroup = "testGroup";

        PopRequest request = createPopRequest(topicName, consumerGroup);

        Topic topic = Topic.builder()
            .topicName(topicName)
            .readQueueNums(8)
            .writeQueueNums(8)
            .perm(6)
            .build();

        when(topicFacade.getTopic(topicName)).thenReturn(topic);
        when(subscriptionFacade.getGroup(topicName, consumerGroup)).thenReturn(null);

        assertThrows(InvalidRequestException.class, () -> contextBuilder.build(request));
    }

    @Test
    void testBuildSubscriptionGroupNotConsumable() {
        String topicName = "TestTopic";
        String consumerGroup = "testGroup";

        PopRequest request = createPopRequest(topicName, consumerGroup);

        Topic topic = Topic.builder()
            .topicName(topicName)
            .readQueueNums(8)
            .writeQueueNums(8)
            .perm(6)
            .build();

        SubscriptionGroup subscriptionGroup = SubscriptionGroup.builder()
            .groupName(consumerGroup)
            .consumeEnable(false)
            .build();

        when(topicFacade.getTopic(topicName)).thenReturn(topic);
        when(subscriptionFacade.getGroup(topicName, consumerGroup)).thenReturn(subscriptionGroup);

        assertThrows(InvalidRequestException.class, () -> contextBuilder.build(request));
    }

    private PopRequest createPopRequest(String topicName, String consumerGroup) {
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setTopic(topicName);
        subscriptionData.setSubString("*");

        return PopRequest.builder()
            .topicName(topicName)
            .consumerGroup(consumerGroup)
            .subscriptionData(subscriptionData)
            .requestContext(RequestContext.builder().build())
            .build();
    }
}
