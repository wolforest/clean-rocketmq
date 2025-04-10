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
package cn.coderule.minimq.domain.domain.dto;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * timer task to be scheduled, build from ConsumeQueue and CommitLog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerRequest implements Serializable {

    /**
     * commitLog offset
     * @renamed from offsetPy to commitLogOffset
     */
    private long commitLogOffset;
    /**
     * size of message in the commitLog
     * @renamed from sizePy to messageSize
     */
    private int messageSize;
    /**
     * delayTime of message, stored in message property map
     */
    private long delayTime;
    /**
     * magic code, always equals TimerMessageAccepter.MAGIC_DEFAULT (1)
     */
    private int magic;

    /**
     * enqueue timestamp (ms)
     */
    private long enqueueTime;
    /**
     * timer task related msg
     */
    private MessageBO messageBO;

    //optional would be a good choice, but it relies on JDK 8
    private CountDownLatch latch;

    private boolean released;

    //whether the operation is successful
    private boolean success;

    private Set<String> deleteList;

    @Override
    public String toString() {
        return "TimerRequest{" +
            "offsetPy=" + commitLogOffset +
            ", sizePy=" + messageSize +
            ", delayTime=" + delayTime +
            ", enqueueTime=" + enqueueTime +
            ", magic=" + magic +
            ", msg=" + messageBO +
            ", latch=" + latch +
            ", released=" + released +
            ", succ=" + success +
            ", deleteList=" + deleteList +
            '}';
    }
}
