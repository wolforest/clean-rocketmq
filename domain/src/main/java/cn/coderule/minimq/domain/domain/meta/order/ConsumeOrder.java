package cn.coderule.minimq.domain.domain.meta.order;

import cn.coderule.minimq.domain.utils.ExtraInfoUtils;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static cn.coderule.minimq.domain.domain.meta.order.OrderUtils.buildKey;

public class ConsumeOrder implements Serializable {
    private static final long CLEAN_SPAN_FROM_LAST = 24 * 3600 * 1000;

    private final ConcurrentMap<String, ConcurrentMap<Integer, OrderInfo>> orderTree;

    public ConsumeOrder() {
        this.orderTree = new ConcurrentHashMap<>();
    }

    public ConcurrentMap<Integer, OrderInfo> getOrderMap(String key) {
        return orderTree.computeIfAbsent(
            key, k -> new ConcurrentHashMap<>(16)
        );
    }

    public boolean isLocked(OrderRequest request) {
        ConcurrentMap<Integer, OrderInfo> map = getOrderMap(request.getKey());

        OrderInfo orderInfo = map.get(request.getQueueId());
        if (orderInfo == null) {
            return false;
        }

        return orderInfo.isLocked(
            request.getAttemptId(),
            request.getInvisibleTime()
        );
    }

    public void clearLock(String topicName, String consumerGroup, int queueId) {
        String key = buildKey(topicName, consumerGroup);
        orderTree.computeIfPresent(key, (k, v) -> {
           v.remove(queueId);
           return v;
        });
    }

    public void update(OrderRequest request) {
        ConcurrentMap<Integer, OrderInfo> map = getOrderMap(request.getKey());

        OrderInfo orderInfo = updateOrderInfo(map, request);
        int minConsumeTimes = updateOffsetCount(orderInfo, request);

    }

    private int updateOffsetCount(OrderInfo orderInfo,  OrderRequest request) {
        Map<Long, Integer> offsetCountMap = orderInfo.getOffsetConsumedCount();
        if (null == offsetCountMap) {
            return  0;
        }

        if (offsetCountMap.size() != orderInfo.countOffsetList()) {
            return 0;
        }

        int minCount = Integer.MAX_VALUE;
        for (Long offset : offsetCountMap.keySet()) {
            Integer consumeTimes = offsetCountMap.getOrDefault(offset, 0);
            ExtraInfoUtils.buildQueueOffsetOrderCountInfo(
                request.orderInfoBuilder, request.getTopicName(), request.getQueueId(), offset, consumeTimes
            );
            minCount = Math.min(minCount, consumeTimes);
        }

        return minCount;
    }

    private OrderInfo updateOrderInfo(ConcurrentMap<Integer, OrderInfo> map, OrderRequest request) {
        OrderInfo orderInfo = map.get(request.getQueueId());
        OrderInfo newOrderInfo = OrderConverter.toOrderInfo(request);
        if (orderInfo != null) {
            newOrderInfo.mergeOffsetConsumedCount(
                orderInfo.getAttemptId(),
                orderInfo.getOffsetList(),
                orderInfo.getOffsetConsumedCount()
            );
        }

        orderInfo = newOrderInfo;
        map.put(request.getQueueId(), orderInfo);

        return orderInfo;
    }

    private void updateLockFreeTimestamp(String topic, String group, int queueId, OrderInfo orderInfo) {

    }
}
