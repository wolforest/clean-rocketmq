package cn.coderule.wolfmq.domain.domain.consumer;

import cn.coderule.wolfmq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.wolfmq.domain.core.enums.consume.ConsumeType;
import cn.coderule.wolfmq.domain.core.enums.message.MessageModel;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumerInfoTest {

    @Test
    void testBuilder_createsConsumerInfo() {
        RequestContext context = RequestContext.builder().build();

        ConsumerInfo info = ConsumerInfo.builder()
            .requestContext(context)
            .groupName("testGroup")
            .messageModel(MessageModel.CLUSTERING)
            .consumeType(ConsumeType.CONSUME_PASSIVELY)
            .consumeStrategy(ConsumeStrategy.CONSUME_FROM_START)
            .build();

        assertNotNull(info);
        assertEquals("testGroup", info.getGroupName());
        assertEquals(MessageModel.CLUSTERING, info.getMessageModel());
    }

    @Test
    void testGetTopicSet_returnsTopicsFromSubscription() {
        SubscriptionData sub1 = mock(SubscriptionData.class);
        when(sub1.getTopic()).thenReturn("topic1");

        SubscriptionData sub2 = mock(SubscriptionData.class);
        when(sub2.getTopic()).thenReturn("topic2");

        Set<SubscriptionData> subscriptionSet = new TreeSet<>();
        subscriptionSet.add(sub1);
        subscriptionSet.add(sub2);

        ConsumerInfo info = ConsumerInfo.builder()
            .subscriptionSet(subscriptionSet)
            .build();

        Set<String> topics = info.getTopicSet();

        assertEquals(2, topics.size());
        assertTrue(topics.contains("topic1"));
        assertTrue(topics.contains("topic2"));
    }

    @Test
    void testToGroupInfo_createsConsumerGroupInfo() {
        ConsumerInfo info = ConsumerInfo.builder()
            .groupName("testGroup")
            .consumeType(ConsumeType.CONSUME_PASSIVELY)
            .messageModel(MessageModel.CLUSTERING)
            .consumeStrategy(ConsumeStrategy.CONSUME_FROM_START)
            .build();

        cn.coderule.wolfmq.domain.domain.consumer.running.ConsumerGroupInfo groupInfo = info.toGroupInfo();

        assertNotNull(groupInfo);
        assertEquals(ConsumeType.CONSUME_PASSIVELY, groupInfo.getConsumeType());
        assertEquals(MessageModel.CLUSTERING, groupInfo.getMessageModel());
    }

    @Test
    void testDefaultValues() {
        ConsumerInfo info = ConsumerInfo.builder().build();

        assertNotNull(info.getSubscriptionSet());
        assertFalse(info.isEnableNotification());
        assertFalse(info.isEnableModification());
    }
}