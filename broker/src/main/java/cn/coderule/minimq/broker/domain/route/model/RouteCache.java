package cn.coderule.minimq.broker.domain.route.model;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Map;
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

    }

    public void updateSubscription(String topicName, RouteInfo routeInfo) {

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
}
