package cn.coderule.minimq.store.domain.meta;

import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.domain.meta.order.ConsumeOrder;
import cn.coderule.minimq.domain.domain.meta.order.OrderInfo;
import cn.coderule.minimq.domain.domain.meta.order.OrderUtils;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.cluster.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.domain.cluster.store.domain.meta.TopicService;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderLockCleaner {
    private static final long CLEAN_SPAN_FROM_LAST = 24 * 3600 * 1000;

    private TopicService topicService;
    private SubscriptionService subscriptionService;

    public void clean(ConsumeOrder consumeOrder) {
        Iterator<
            Map.Entry<
                String, ConcurrentMap<Integer, OrderInfo>
                >
            > iterator = consumeOrder.getIterator();

        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            Map.Entry<
                String, ConcurrentMap<Integer, OrderInfo>
                > entry = iterator.next();

            String[] arr = OrderUtils.decodeKey(entry.getKey());
            if (arr.length != 2) continue;

            Topic topic = topicService.getTopic(arr[0]);
            if (!validateTopic(topic, iterator)) continue;

            if (!validateGroup(arr[1], iterator)) continue;
            if (!validateOrderMap(entry.getValue(), iterator)) continue;

            Iterator<
                Map.Entry<Integer, OrderInfo>
                > mapIterator = entry.getValue()
                .entrySet()
                .iterator();

            cleanOrderMap(mapIterator, topic, now);
        }
    }

    private void cleanOrderMap(Iterator<Map.Entry<Integer, OrderInfo>> mapIterator, Topic topic, long now) {
        while (mapIterator.hasNext()) {
            Map.Entry<Integer, OrderInfo> mapEntry = mapIterator.next();

            if (!validateQueue(topic, mapEntry.getKey(), mapIterator)) continue;

            validateConsumeTime(mapEntry.getValue(), now, mapIterator);
        }
    }

    private boolean validateTopic(Topic topic, Iterator<Map.Entry<String, ConcurrentMap<Integer, OrderInfo>>> iterator) {
        if (topic != null) {
            return true;
        }

        log.info("Topic is null, remove topic while cleanExpiredLock");
        iterator.remove();
        return false;
    }

    private boolean validateGroup(String groupName, Iterator<Map.Entry<String, ConcurrentMap<Integer, OrderInfo>>> iterator) {
        SubscriptionGroup group = subscriptionService.getGroup(groupName);
        if (group != null) {
            return true;
        }

        log.info("Group is null, remove group while cleanExpiredLock");
        iterator.remove();
        return false;
    }

    private boolean validateOrderMap(ConcurrentMap<Integer, OrderInfo> map, Iterator<Map.Entry<String, ConcurrentMap<Integer, OrderInfo>>> iterator) {
        if (MapUtil.notEmpty(map)) {
            return true;
        }

        log.info("OrderMap is empty, remove group while cleanExpiredLock");
        iterator.remove();
        return false;
    }

    private boolean validateQueue(Topic topic, int queueId, Iterator<Map.Entry<Integer, OrderInfo>> mapIterator) {
        if (queueId < topic.getReadQueueNums()) {
            return true;
        }

        log.info("QueueId is invalid, remove queue while cleanExpiredLock");
        mapIterator.remove();
        return false;
    }

    private void validateConsumeTime(OrderInfo orderInfo, long now, Iterator<Map.Entry<Integer, OrderInfo>> mapIterator) {
        if (now - orderInfo.getLastConsumeTimestamp() <= CLEAN_SPAN_FROM_LAST) {
            return;
        }

        log.info("last consume time greater than {}ms while cleanExpiredLock, orderInfo={}",
            CLEAN_SPAN_FROM_LAST, orderInfo);
        mapIterator.remove();
    }

}
