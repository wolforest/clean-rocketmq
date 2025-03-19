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
package cn.coderule.minimq.domain.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import cn.coderule.minimq.domain.enums.TagType;
import cn.coderule.minimq.domain.enums.MessageType;
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
    private static final TypeReference<Map<String, String>> ATTRIBUTES_TYPE_REFERENCE = new TypeReference<>() {
    };

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

    public Topic(Topic other) {
        this.topicName = other.topicName;
        this.readQueueNums = other.readQueueNums;
        this.writeQueueNums = other.writeQueueNums;
        this.perm = other.perm;
        this.tagType = other.tagType;
        this.topicSysFlag = other.topicSysFlag;
        this.order = other.order;
        this.attributes = other.attributes;
    }

    @JSONField(serialize = false, deserialize = false)
    public MessageType getTopicType() {
        if (attributes == null) {
            return MessageType.NORMAL;
        }
        String content = attributes.get(MESSAGE_TYPE_KEY);
        if (content == null) {
            return MessageType.NORMAL;
        }
        return MessageType.valueOf(content);
    }

    @JSONField(serialize = false, deserialize = false)
    public void setTopicType(MessageType messageType) {
        attributes.put(MESSAGE_TYPE_KEY, messageType.getValue());
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        //[0]
        sb.append(this.topicName);
        sb.append(SEPARATOR);
        //[1]
        sb.append(this.readQueueNums);
        sb.append(SEPARATOR);
        //[2]
        sb.append(this.writeQueueNums);
        sb.append(SEPARATOR);
        //[3]
        sb.append(this.perm);
        sb.append(SEPARATOR);
        //[4]
        sb.append(this.tagType);
        sb.append(SEPARATOR);
        //[5]
        if (attributes != null) {
            sb.append(JSON.toJSONString(attributes));
        }

        return sb.toString();
    }

    public boolean decode(final String in) {
        String[] strs = in.split(SEPARATOR);
        if (strs.length < 5) {
            return false;
        }

        this.topicName = strs[0];
        this.readQueueNums = Integer.parseInt(strs[1]);
        this.writeQueueNums = Integer.parseInt(strs[2]);
        this.perm = Integer.parseInt(strs[3]);
        this.tagType = TagType.valueOf(strs[4]);
        decodeAttributes(strs);

        return true;
    }

    private void decodeAttributes(String[] strs) {
        if (strs.length < 6) {
            return;
        }

        try {
            this.attributes = JSON.parseObject(strs[5], ATTRIBUTES_TYPE_REFERENCE.getType());
        } catch (Exception e) {
            // ignore exception when parse failed, cause map's key/value can have ' ' char.
        }
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
