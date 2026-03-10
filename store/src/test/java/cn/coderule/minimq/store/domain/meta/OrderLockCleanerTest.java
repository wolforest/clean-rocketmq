package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.domain.meta.order.ConsumeOrder;
import cn.coderule.minimq.domain.domain.meta.order.OrderInfo;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.domain.meta.order.OrderUtils;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.domain.store.domain.meta.TopicService;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderLockCleanerTest {

    @Test
    void clean_WhenTopicMissing_ShouldRemoveEntry() {
        TopicService topicService = mock(TopicService.class);
        SubscriptionService subscriptionService = mock(SubscriptionService.class);
        OrderLockCleaner cleaner = new OrderLockCleaner(topicService, subscriptionService);

        ConsumeOrder consumeOrder = new ConsumeOrder();
        String key = OrderUtils.buildKey("topic-a", "group-a");
        consumeOrder.getOrderMap(key).put(0, createOrderInfo(System.currentTimeMillis()));
        when(topicService.getTopic("topic-a")).thenReturn(null);

        cleaner.clean(consumeOrder);

        assertEquals(0, entryCount(consumeOrder));
    }

    @Test
    void clean_WhenQueueInvalidOrExpired_ShouldRemoveQueueEntry() {
        TopicService topicService = mock(TopicService.class);
        SubscriptionService subscriptionService = mock(SubscriptionService.class);
        OrderLockCleaner cleaner = new OrderLockCleaner(topicService, subscriptionService);

        ConsumeOrder consumeOrder = new ConsumeOrder();
        String key = OrderUtils.buildKey("topic-b", "group-b");
        consumeOrder.getOrderMap(key).put(2, createOrderInfo(System.currentTimeMillis()));
        consumeOrder.getOrderMap(key).put(0, createOrderInfo(System.currentTimeMillis() - 3L * 24 * 3600 * 1000));

        Topic topic = new Topic();
        topic.setTopicName("topic-b");
        topic.setReadQueueNums(1);
        when(topicService.getTopic("topic-b")).thenReturn(topic);
        when(subscriptionService.getGroup("group-b")).thenReturn(new SubscriptionGroup());

        cleaner.clean(consumeOrder);

        assertTrue(consumeOrder.getOrderMap(key).isEmpty());
    }

    @Test
    void clean_WhenEntryValid_ShouldKeepOrderInfo() {
        TopicService topicService = mock(TopicService.class);
        SubscriptionService subscriptionService = mock(SubscriptionService.class);
        OrderLockCleaner cleaner = new OrderLockCleaner(topicService, subscriptionService);

        ConsumeOrder consumeOrder = new ConsumeOrder();
        String key = OrderUtils.buildKey("topic-c", "group-c");
        consumeOrder.getOrderMap(key).put(0, createOrderInfo(System.currentTimeMillis()));

        Topic topic = new Topic();
        topic.setTopicName("topic-c");
        topic.setReadQueueNums(1);
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName("group-c");
        when(topicService.getTopic("topic-c")).thenReturn(topic);
        when(subscriptionService.getGroup("group-c")).thenReturn(group);

        cleaner.clean(consumeOrder);

        assertEquals(1, consumeOrder.getOrderMap(key).size());
    }

    private OrderInfo createOrderInfo(long lastConsumeTimestamp) {
        return OrderInfo.builder()
            .lastConsumeTimestamp(lastConsumeTimestamp)
            .offsetList(List.of(10L))
            .commitOffsetBit(0L)
            .build();
    }

    private int entryCount(ConsumeOrder consumeOrder) {
        int count = 0;
        Iterator<Map.Entry<String, ConcurrentMap<Integer, OrderInfo>>> iterator = consumeOrder.getIterator();
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        return count;
    }

    @SuppressWarnings("unused")
    private OrderRequest createRequest(String topic, String group, int queueId) {
        return OrderRequest.builder()
            .topicName(topic)
            .consumerGroup(group)
            .queueId(queueId)
            .build();
    }
}

