/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wolf.minimq.domain.utils.lock;

import com.wolf.minimq.domain.model.MessageQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Message Queue lock, strictly ensure
 *  - each queue
 *  - only consumed by one thread
 *  - at the same time
 *  - used in client
 */
public class MessageQueueLock {
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
