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

/**
 * $Id: CreateTopicRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package cn.coderule.minimq.rpc.store.protocol.header;

import cn.coderule.minimq.domain.domain.enums.TagType;
import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.utils.attribute.AttributeParser;
import cn.coderule.minimq.rpc.common.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.core.annotation.CFNullable;
import cn.coderule.minimq.rpc.common.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.core.annotation.RocketMQResource;
import cn.coderule.minimq.rpc.common.core.enums.Action;
import cn.coderule.minimq.rpc.common.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.header.RpcRequestHeader;
import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@RocketMQAction(value = RequestCode.UPDATE_AND_CREATE_TOPIC, action = Action.CREATE)
public class CreateTopicRequestHeader extends RpcRequestHeader {
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    @CFNotNull
    private String defaultTopic;
    @CFNotNull
    private Integer readQueueNums;
    @CFNotNull
    private Integer writeQueueNums;
    @CFNotNull
    private Integer perm;
    @CFNotNull
    private String topicFilterType;
    private Integer topicSysFlag;
    @CFNotNull
    private Boolean order = false;
    private String attributes;

    @CFNullable
    private Boolean force = false;

    private Boolean lo;

    public Topic toTopic() {
        Topic topic = new Topic();
        topic.setTopicName(this.topic);
        topic.setReadQueueNums(this.readQueueNums);
        topic.setWriteQueueNums(this.writeQueueNums);
        topic.setPerm(this.perm);
        topic.setOrder(this.order);

        int sysFlag = null != this.topicSysFlag ? this.topicSysFlag : 0;
        topic.setTopicSysFlag(sysFlag);

        topic.setAttributes(AttributeParser.toMap(this.attributes));

        return topic;
    }

    @Override
    public void checkFields() throws RemotingCommandException {
        try {
            TagType.valueOf(this.topicFilterType);
        } catch (Exception e) {
            throw new RemotingCommandException("topicFilterType = [" + topicFilterType + "] value invalid", e);
        }
    }

    public TagType getTopicFilterTypeEnum() {
        return TagType.valueOf(this.topicFilterType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("topic", topic)
            .add("defaultTopic", defaultTopic)
            .add("readQueueNums", readQueueNums)
            .add("writeQueueNums", writeQueueNums)
            .add("perm", perm)
            .add("topicFilterType", topicFilterType)
            .add("topicSysFlag", topicSysFlag)
            .add("order", order)
            .add("attributes", attributes)
            .add("force", force)
            .toString();
    }
}
