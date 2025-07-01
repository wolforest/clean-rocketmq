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
package cn.coderule.minimq.broker.domain.transaction.model;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class OffsetQueue implements Serializable {
    private final AtomicInteger totalSize;

    private volatile long lastWriteTime;

    /**
     * queue data:
     *  offset1, offset2, offsetN
     */
    private final LinkedBlockingQueue<String> queue;

    public OffsetQueue(long timestamp, int queueLength) {
        this.lastWriteTime = timestamp;
        queue = new LinkedBlockingQueue<>(queueLength);
        totalSize = new AtomicInteger(0);
    }

    public boolean isEmpty() {
        return totalSize.get() <= 0
                || queue.isEmpty();
    }

    public String poll() {
        return queue.poll();
    }

    public int addAndGet(int delta) {
        int size = totalSize.addAndGet(delta);
        lastWriteTime = System.currentTimeMillis();
        return size;
    }

    public boolean offer(String data, long timeout) {
        try {
            boolean result = queue.offer(data, timeout, TimeUnit.MILLISECONDS);
            totalSize.addAndGet(data.length());

            return result;
        } catch (InterruptedException ignore) {
        }

        return false;
    }

    public int getTotalSize() {
        return totalSize.get();
    }
}
