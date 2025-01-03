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

package com.wolf.minimq.domain.enums;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;

public enum TopicType {
    UNSPECIFIED("UNSPECIFIED"),
    NORMAL("NORMAL"),
    FIFO("FIFO"),
    DELAY("DELAY"),
    TRANSACTION("TRANSACTION"),
    MIXED("MIXED");

    private final String value;
    TopicType(String value) {
        this.value = value;
    }

    public static Set<String> topicMessageTypeSet() {
        return Sets.newHashSet(UNSPECIFIED.value, NORMAL.value, FIFO.value, DELAY.value, TRANSACTION.value, MIXED.value);
    }

    public String getValue() {
        return value;
    }

    public static TopicType parseFromMessageProperty(Map<String, String> messageProperty) {
        String isTrans = messageProperty.get(MessageConst.PROPERTY_TRANSACTION_PREPARED);
        String isTransValue = "true";
        if (isTransValue.equals(isTrans)) {
            return TopicType.TRANSACTION;
        } else if (messageProperty.get(MessageConst.PROPERTY_DELAY_TIME_LEVEL) != null
            || messageProperty.get(MessageConst.PROPERTY_TIMER_DELIVER_MS) != null
            || messageProperty.get(MessageConst.PROPERTY_TIMER_DELAY_SEC) != null) {
            return TopicType.DELAY;
        } else if (messageProperty.get(MessageConst.PROPERTY_SHARDING_KEY) != null) {
            return TopicType.FIFO;
        }
        return TopicType.NORMAL;
    }

    public String getMetricsValue() {
        return value.toLowerCase();
    }
}
