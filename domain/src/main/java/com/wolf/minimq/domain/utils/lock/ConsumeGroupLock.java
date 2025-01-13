package com.wolf.minimq.domain.utils.lock;

import com.wolf.common.lang.concurrent.ServiceThread;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeGroupLock extends ServiceThread {
    private static final int CLEAN_INTERVAL = 1000 * 60;
    private static final int CLEAN_TIMEOUT = 1000 * 60;

    private final ConcurrentMap<String, TimedLock> lockMap = new ConcurrentHashMap<>(100_000);

    @Override
    public String getServiceName() {
        return ConsumeQueueLock.class.getSimpleName();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            try {
                this.await(CLEAN_INTERVAL);
                cleanUnusedLock();
            } catch (Exception e) {
                log.error("{} service has exception. ", this.getServiceName(), e);
            }
        }
    }

    public void lock(String group, String topic, int queueId) {
        while (true) {
            if (tryLock(group, topic, queueId)) {
                break;
            }
        }
    }

    public boolean tryLock(String group, String topic, int queueId) {
        String key = createKey(group, topic, queueId);
        TimedLock lock = getLock(key);
        if (lock == null) {
            return false;
        }

        return lock.tryLock();
    }

    public void unlock(String group, String topic, int queueId) {
        String key = createKey(group, topic, queueId);
        TimedLock lock = getLock(key);
        if (lock == null) {
            return;
        }

        lock.unLock();
    }

    private String createKey(String group, String topic, int queueId) {
        return group + "@" + topic + "@" + queueId;
    }

    private TimedLock getLock(String key) {
        TimedLock lock = lockMap.get(key);

        if (lock == null) {
            lock = new TimedLock();
            TimedLock old = lockMap.putIfAbsent(key, lock);
            if (old != null) {
                return null;
            }
        }

        return lock;
    }

    private void cleanUnusedLock() {
        Iterator<Map.Entry<String, TimedLock>> iterator = lockMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, TimedLock> entry = iterator.next();

            long lockedTime = System.currentTimeMillis() - entry.getValue().getLockTime();
            if (lockedTime <= ConsumeGroupLock.CLEAN_TIMEOUT) {
                continue;
            }

            iterator.remove();
            log.info("Remove unused queue lock: {}, {}, {}",
                entry.getKey(), entry.getValue().getLockTime(), entry.getValue().isLocked());
        }
    }

}
