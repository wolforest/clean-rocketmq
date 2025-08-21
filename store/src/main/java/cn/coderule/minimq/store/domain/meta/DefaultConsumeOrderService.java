package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.domain.meta.order.ConsumeOrder;
import cn.coderule.minimq.domain.domain.meta.order.OrderInfo;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.domain.meta.order.OrderUtils;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOrderService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class DefaultConsumeOrderService implements ConsumeOrderService {
    private static final long CLEAN_SPAN_FROM_LAST = 24 * 3600 * 1000;

    private ConsumeOrder consumeOrder;
    private TopicService topicService;

    @Override
    public void lock(OrderRequest request) {
        consumeOrder.lock(request);
    }

    @Override
    public long commit(OrderRequest request) {
        return consumeOrder.commit(request);
    }

    @Override
    public boolean isLocked(OrderRequest request) {
        return consumeOrder.isLocked(request);
    }

    @Override
    public void unlock(OrderRequest request) {
        consumeOrder.unlock(
            request.getTopicName(),
            request.getConsumerGroup(),
            request.getQueueId()
        );
    }

    @Override
    public void updateInvisible(OrderRequest request) {
        consumeOrder.updateVisible(request);
    }

    @Override
    public void load() {

    }

    @Override
    public void store() {

    }

    private void autoClean() {
        Iterator<
            Map.Entry<
                String, ConcurrentMap<Integer, OrderInfo>
            >
        > iterator = consumeOrder.getIterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ConcurrentMap<Integer, OrderInfo>> entry
                = iterator.next();

            String[] arr = OrderUtils.decodeKey(entry.getKey());
            if (arr.length != 2) continue;

            Topic topic = topicService.getTopic(arr[0]);
            if (!validateTopic(topic, iterator)) continue;

            if (!validateGroup(arr[1], iterator)) continue;
            if (!validateOrderMap(entry.getValue(), iterator)) continue;

            Iterator<Map.Entry<Integer, OrderInfo>> mapIterator
                = entry.getValue().entrySet().iterator();
            cleanMap(mapIterator, topic);
        }
    }

    private void cleanMap(Iterator<Map.Entry<Integer, OrderInfo>> mapIterator, Topic topic) {
        while (mapIterator.hasNext()) {
            Map.Entry<Integer, OrderInfo> mapEntry = mapIterator.next();

            if (!validateQueue(topic, mapEntry.getKey(), mapIterator)) continue;

            validateConsumeTime(mapEntry.getValue(), mapIterator);
        }
    }

    private boolean validateQueue(Topic topic, int queueId, Iterator<Map.Entry<Integer, OrderInfo>> mapIterator) {
        return true;
    }

    private boolean validateTopic(Topic topic, Iterator<Map.Entry<String, ConcurrentMap<Integer, OrderInfo>>> iterator) {
        return true;
    }

    private boolean validateGroup(String groupName, Iterator<Map.Entry<String, ConcurrentMap<Integer, OrderInfo>>> iterator) {
        return true;
    }

    private boolean validateOrderMap(ConcurrentMap<Integer, OrderInfo> map, Iterator<Map.Entry<String, ConcurrentMap<Integer, OrderInfo>>> iterator) {
        return true;
    }

    private void validateConsumeTime(OrderInfo orderInfo, Iterator<Map.Entry<Integer, OrderInfo>> mapIterator) {

    }

}
