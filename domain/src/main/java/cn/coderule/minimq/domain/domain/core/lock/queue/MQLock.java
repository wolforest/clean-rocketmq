package cn.coderule.minimq.domain.domain.core.lock.queue;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Message Queue lock, strictly ensure
 *  - each queue
 *  - only consumed by one thread
 *  - at the same time
 *  - used in client
 */
public class MQLock {
    private final ConcurrentMap<MessageQueue, ConcurrentMap<Integer, Object>> lockMap =
        new ConcurrentHashMap<>(32);

    public Object getLock(final MessageQueue mq) {
        return getLock(mq, -1);
    }

    public Object getLock(final MessageQueue mq, final int shardingKey) {
        ConcurrentMap<Integer, Object> objMap = getLockMap(mq);
        return getLockByKey(objMap, shardingKey);
    }

    private Object getLockByKey(ConcurrentMap<Integer, Object> objMap, final int shardingKey) {
        Object lock = objMap.get(shardingKey);
        if (null != lock) {
            return lock;
        }

        lock = new Object();
        Object prevLock = objMap.putIfAbsent(shardingKey, lock);
        if (prevLock != null) {
            lock = prevLock;
        }

        return lock;
    }

    private ConcurrentMap<Integer, Object> getLockMap(final MessageQueue mq) {
        ConcurrentMap<Integer, Object> objMap = this.lockMap.get(mq);
        if (null != objMap) {
            return objMap;
        }

        objMap = new ConcurrentHashMap<>(32);
        ConcurrentMap<Integer, Object> prevObjMap = this.lockMap.putIfAbsent(mq, objMap);
        if (prevObjMap != null) {
            objMap = prevObjMap;
        }

        return objMap;
    }
}
