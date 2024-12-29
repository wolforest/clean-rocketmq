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

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;

public class TimedLock {
    private final AtomicBoolean lock;
    @Getter
    private volatile long lockTime;

    public TimedLock() {
        this.lock = new AtomicBoolean(true);
        this.lockTime = System.currentTimeMillis();
    }

    public boolean tryLock() {
        boolean ret = lock.compareAndSet(true, false);
        if (!ret) {
            return false;
        }

        this.lockTime = System.currentTimeMillis();
        return true;
    }

    public void unLock() {
        lock.set(true);
    }

    public boolean isLocked() {
        return !lock.get();
    }

}
