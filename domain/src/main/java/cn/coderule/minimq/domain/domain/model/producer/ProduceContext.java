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
package cn.coderule.minimq.domain.domain.model.producer;

import cn.coderule.minimq.domain.domain.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import java.io.Serializable;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduceContext implements Serializable {
    private RequestContext requestContext;
    private MessageBO messageBO;
    private MessageQueue messageQueue;
    private Topic topic;

    /** namespace */
    private String namespace;
    /** producer group without namespace. */
    private String producerGroup;
    /** topic without namespace. */
    private String topicName;
    private String msgId;
    private String originMsgId;
    private Integer queueId;
    private Long queueOffset;
    private String brokerAddr;
    private String bornHost;
    private int bodyLength;
    private int code;
    private String errorMsg;
    private String msgProps;
    private Object mqTraceContext;
    private Properties extProps;
    private String brokerRegionId;
    private String msgUniqueKey;
    private long bornTimeStamp;
    private long requestTimeStamp;
    @Builder.Default
    private MessageType msgType = MessageType.COMMIT;

    @Builder.Default
    private boolean success = false;

    /**
     * Account Statistics
     */
    private String accountAuthType;
    private String accountOwnerParent;
    private String accountOwnerSelf;
    private int sendMsgNum;
    private int sendMsgSize;


    public static ProduceContext from(RequestContext requestContext, MessageBO messageBO, MessageQueue messageQueue) {
        return ProduceContext.builder()
            .requestContext(requestContext)
            .messageBO(messageBO)
            .messageQueue(messageQueue)
            .build();
    }

}
