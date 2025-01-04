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
package com.wolf.minimq.domain.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import com.wolf.minimq.domain.enums.TagType;
import com.wolf.minimq.domain.enums.TopicType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topic implements Serializable {
    private static final String SEPARATOR = " ";
    private static final String MESSAGE_TYPE_KEY = "+message.type";
    public static final int DEFAULT_READ_QUEUE_NUMS = 16;
    public static final int DEFAULT_WRITE_QUEUE_NUMS = 16;

    private String topicName;
    @Builder.Default
    private int readQueueNums = DEFAULT_READ_QUEUE_NUMS;
    @Builder.Default
    private int writeQueueNums = DEFAULT_WRITE_QUEUE_NUMS;
    @Builder.Default
    private int perm = 0;
    @Builder.Default
    private TagType tagType = TagType.SINGLE_TAG;
    @Builder.Default
    private int topicSysFlag = 0;
    @Builder.Default
    private boolean order = false;

    /**
     * Field attributes key should start with '+', and should not have ' ' char in key or value
     * topicType stored in attributes, key = '+message.type'
     * put("+message.type", TopicMessageType.FIFO.getValue());
     * put("+message.type", TopicMessageType.DELAY.getValue());
     * put("+message.type", TopicMessageType.TRANSACTION.getValue());
     *
     */
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    @JSONField(serialize = false, deserialize = false)
    public TopicType getTopicType() {
        if (attributes == null) {
            return TopicType.NORMAL;
        }
        String content = attributes.get(MESSAGE_TYPE_KEY);
        if (content == null) {
            return TopicType.NORMAL;
        }
        return TopicType.valueOf(content);
    }

    @JSONField(serialize = false, deserialize = false)
    public void setTopicType(TopicType topicType) {
        attributes.put(MESSAGE_TYPE_KEY, topicType.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Topic that = (Topic) o;

        if (readQueueNums != that.readQueueNums) {
            return false;
        }
        if (writeQueueNums != that.writeQueueNums) {
            return false;
        }
        if (perm != that.perm) {
            return false;
        }
        if (topicSysFlag != that.topicSysFlag) {
            return false;
        }
        if (order != that.order) {
            return false;
        }
        if (!Objects.equals(topicName, that.topicName)) {
            return false;
        }
        if (tagType != that.tagType) {
            return false;
        }
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        int result = topicName != null ? topicName.hashCode() : 0;
        result = 31 * result + readQueueNums;
        result = 31 * result + writeQueueNums;
        result = 31 * result + perm;
        result = 31 * result + (tagType != null ? tagType.hashCode() : 0);
        result = 31 * result + topicSysFlag;
        result = 31 * result + (order ? 1 : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Topic [topicName=" + topicName + ", readQueueNums=" + readQueueNums
            + ", writeQueueNums=" + writeQueueNums + ", perm=0"
            + ", topicFilterType=" + tagType + ", topicSysFlag=" + topicSysFlag + ", order=" + order
            + ", attributes=" + attributes + "]";
    }
}
