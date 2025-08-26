package cn.coderule.minimq.domain.domain.meta.order;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

import static cn.coderule.minimq.domain.domain.meta.order.OrderUtils.buildKey;

@Slf4j
public class ConsumeOrder implements Serializable {
    private final ConcurrentMap<String, ConcurrentMap<Integer, OrderInfo>> orderTree;

    public ConsumeOrder() {
        this.orderTree = new ConcurrentHashMap<>();
    }

    public Iterator<Map.Entry<String, ConcurrentMap<Integer, OrderInfo>>> getIterator() {
        return orderTree.entrySet().iterator();
    }

    public ConcurrentMap<Integer, OrderInfo> getOrderMap(String key) {
        return orderTree.computeIfAbsent(
            key, k -> new ConcurrentHashMap<>(16)
        );
    }

    public OrderInfo getOrderInfo(OrderRequest request) {
        ConcurrentMap<Integer, OrderInfo> map = orderTree.get(request.getKey());
        if (map == null) {
            return null;
        }

        return map.get(request.getQueueId());
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

    public void unlock(String topicName, String consumerGroup, int queueId) {
        String key = buildKey(topicName, consumerGroup);
        orderTree.computeIfPresent(key, (k, v) -> {
           v.remove(queueId);
           return v;
        });
    }

    public void lock(OrderRequest request) {
        ConcurrentMap<Integer, OrderInfo> map = getOrderMap(request.getKey());

        OrderInfo orderInfo = updateOrderInfo(map, request);
        // updateOffsetCount(orderInfo, request);

        updateLockFreeTimestamp(orderInfo, request);
    }

    public long commit(OrderRequest request) {
        String key = request.getKey();
        ConcurrentMap<Integer, OrderInfo> map = getOrderMap(key);
        if (map == null) {
            return request.getQueueOffset() + 1;
        }

        OrderInfo orderInfo = map.get(request.getQueueId());
        if (orderInfo == null) {
            log.warn("OrderInfo is null, key={}; offset={};",
                key, request.getQueueOffset());
            return request.getQueueOffset() + 1;
        }

        if (CollectionUtil.isEmpty(orderInfo.getOffsetList())) {
            log.warn("OrderInfo is empty, key={}, offset={}",
                key, request.getQueueOffset());
            return -1;
        }

        if (request.getDequeueTime() != orderInfo.getPopTime()) {
            log.warn("OrderInfo popTime is not equal, key={}, offset={}, popTime={}, orderInfo={}",
                key, request.getQueueOffset(), request.getDequeueTime(), orderInfo);
            return -2;
        }

        return commit(request, orderInfo);
    }

    public void updateVisible(OrderRequest request) {
        String key = request.getKey();
        ConcurrentMap<Integer, OrderInfo> map = getOrderMap(key);
        if (map == null) {
            log.warn("OrderInfo not exist, request={}", request);
            return;
        }

        OrderInfo orderInfo = map.get(request.getQueueId());
        if (orderInfo == null) {
            log.warn("OrderInfo is null, request={};", request);
            return;
        }

        if (request.getDequeueTime() != orderInfo.getPopTime()) {
            log.warn("OrderInfo popTime is not equal, request={}, orderInfo={}",
                request, orderInfo);
            return;
        }

        orderInfo.updateOffsetNextVisibleTime(request.getQueueOffset(), request.getInvisibleTime());
        updateLockFreeTimestamp(orderInfo, request);
    }

    private long commit(OrderRequest request, OrderInfo orderInfo) {
        int i = matchOffset(orderInfo, request);
        if (i >= orderInfo.countOffsetList()) {
            log.warn("can not find commit offset, request: {}, orderInfo: {}"
                , request, orderInfo);
            return -1;
        }

        long newBit = orderInfo.getCommitOffsetBit() | (1L << i);
        orderInfo.setCommitOffsetBit(newBit);
        long nextOffset = orderInfo.getNextOffset();

        updateLockFreeTimestamp(orderInfo, request);
        return nextOffset;
    }

    private int matchOffset(OrderInfo orderInfo, OrderRequest request) {
        List<Long> offsetList = orderInfo.getOffsetList();
        Long first = offsetList.get(0);
        int i = 0, size = offsetList.size();

        for (; i < size; i++) {
            long tmp;
            if (0 == i) {
                tmp = first;
            } else {
                tmp = first + offsetList.get(i);
            }

            if (request.getQueueOffset() == tmp) {
                break;
            }
        }

        return i;
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

    private void updateLockFreeTimestamp(OrderInfo orderInfo, OrderRequest request) {

    }
}
