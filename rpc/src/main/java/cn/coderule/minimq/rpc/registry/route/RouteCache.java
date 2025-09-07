package cn.coderule.minimq.rpc.registry.route;

import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.server.GroupInfo;
import cn.coderule.minimq.domain.domain.cluster.route.PublishInfo;
import cn.coderule.minimq.domain.domain.cluster.route.RouteInfo;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class RouteCache implements Serializable {
    private static final long LOCK_TIMEOUT = 3000L;

    private final Lock lock;

    // topicName -> routeInfo
    private final ConcurrentMap<String, RouteInfo> routeMap;
    // topicName -> Set<messageQueue>
    private final ConcurrentMap<String, Set<MessageQueue>> subscriptionMap;
    // groupName -> groupNo -> address
    private final ConcurrentMap<String, Map<Long, String>> addressMap;
    // topicName -> publishInfo
    private final ConcurrentMap<String, PublishInfo> publishMap;

    public RouteCache() {
        this.routeMap = new ConcurrentHashMap<>();
        this.addressMap = new ConcurrentHashMap<>();
        this.subscriptionMap = new ConcurrentHashMap<>();
        this.publishMap = new ConcurrentHashMap<>();

        this.lock = new ReentrantLock();
    }

    public boolean tryLock() {
        try {
            return lock.tryLock(LOCK_TIMEOUT, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("RouteCache tryLock exception: ", e);
            return false;
        }
    }

    public void unlock() {
        lock.unlock();
    }

    public Set<String> getTopicSet() {
        Set<String> topicSetForPopAssignment = this.subscriptionMap.keySet();
        Set<String> topicSetForEscapeBridge = this.routeMap.keySet();
        return Sets.union(topicSetForPopAssignment, topicSetForEscapeBridge);
    }

    public boolean containsSubscription(String topicName) {
        return this.subscriptionMap.containsKey(topicName);
    }

    public boolean containsRoute(String topicName) {
        return this.routeMap.containsKey(topicName);
    }

    public void removeSubscription(String topicName) {
        this.subscriptionMap.remove(topicName);
    }

    public void updateRoute(String topicName, RouteInfo routeInfo) {
        RouteInfo oldRoute = this.routeMap.get(topicName);
        if (!oldRoute.isChanged(routeInfo) && isPublishInfoOk(topicName)) {
            return;
        }

        log.info("update routeInfo, topic: {}, old: {}, new: {}", topicName, oldRoute, routeInfo);

        updateAddressMap(routeInfo);
        updatePublishInfo(topicName, routeInfo);
        updateRouteMap(topicName, routeInfo);
    }

    public void updateSubscription(String topicName, RouteInfo routeInfo) {
        RouteInfo tmp = new RouteInfo(routeInfo);
        tmp.setTopicQueueMappingByBroker(null);

        Set<MessageQueue> newQueueSet = RouteConverter.getQueueSet(topicName, tmp);
        Set<MessageQueue> oldQueueSet = this.subscriptionMap.get(topicName);

        if (Objects.equals(newQueueSet, oldQueueSet)) {
            return;
        }

        this.subscriptionMap.put(topicName, newQueueSet);
    }

    public RouteInfo getRoute(String topicName) {
        return this.routeMap.get(topicName);
    }

    public PublishInfo getPublishInfo(String topicName) {
        return this.publishMap.get(topicName);
    }

    public Set<MessageQueue> getSubscription(String topicName) {
        return this.subscriptionMap.get(topicName);
    }

    public String getAddress(String groupName, long groupNo) {
        Map<Long, String> noMap = this.addressMap.get(groupName);
        if (noMap == null) {
            return null;
        }

        return noMap.get(groupNo);
    }

    public String getFirstAddress(String groupName) {
        Map<Long, String> noMap = this.addressMap.get(groupName);
        if (noMap == null) {
            return null;
        }

        return noMap.values()
            .stream()
            .findFirst()
            .orElse(null);
    }

    private void updateAddressMap(RouteInfo routeInfo) {
        for (GroupInfo groupInfo : routeInfo.getBrokerDatas()) {
            this.addressMap.put(groupInfo.getBrokerName(), groupInfo.getBrokerAddrs());
        }
    }

    private boolean isPublishInfoOk(String topicName) {
        PublishInfo publishInfo = this.publishMap.get(topicName);
        return publishInfo != null && publishInfo.isOk();
    }

    private void updatePublishInfo(String topicName, RouteInfo routeInfo) {
        PublishInfo publishInfo = RouteConverter.toPublishInfo(topicName, routeInfo);
        publishInfo.setHasRoute(true);
        updatePublishInfo(topicName, publishInfo);
    }

    private void updatePublishInfo(String topicName, PublishInfo publishInfo) {
        if (null == topicName || null == publishInfo) {
            return;
        }

        PublishInfo prev = this.publishMap.put(topicName, publishInfo);
        if (prev == null) {
            return;
        }

        log.info("update publishInfo, topic: {}, old: {}, new: {}", topicName, prev, publishInfo);
    }

    private void updateRouteMap(String topicName, RouteInfo routeInfo) {
        RouteInfo newRoute = new RouteInfo(routeInfo);
        this.routeMap.put(topicName, newRoute);
    }

}
