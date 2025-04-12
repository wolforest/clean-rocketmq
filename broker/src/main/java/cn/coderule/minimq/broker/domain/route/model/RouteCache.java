package cn.coderule.minimq.broker.domain.route.model;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
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
    // groupName -> groupNo -> address
    private final ConcurrentMap<String, Map<Long, String>> addressMap;
    // topicName -> Set<messageQueue>
    private final ConcurrentMap<String, Set<MessageQueue>> queueMap;
    // topicName -> publishInfo
    private final ConcurrentMap<String, PublishInfo> publishMap;


    public RouteCache() {
        this.routeMap = new ConcurrentHashMap<>();
        this.addressMap = new ConcurrentHashMap<>();
        this.queueMap = new ConcurrentHashMap<>();
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
}
