package cn.coderule.minimq.domain.domain.meta.order;

import java.io.Serializable;
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
