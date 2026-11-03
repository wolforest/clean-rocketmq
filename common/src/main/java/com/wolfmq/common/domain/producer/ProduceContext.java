package com.wolfmq.common.domain.producer;

import com.wolfmq.common.domain.message.enums.MessageType;
import com.wolfmq.common.domain.mq.MessageQueue;
import com.wolfmq.common.server.broker.RequestContext;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.meta.topic.Topic;
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
    private MessageType msgType = MessageType.NORMAL;

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


    public static ProduceContext from(RequestContext requestContext, MessageBO messageBO) {
        return ProduceContext.builder()
            .requestContext(requestContext)
            .messageBO(messageBO)
            .requestTimeStamp(requestContext.getRequestTime())
            .build();
    }

    public boolean isPrepareMessage() {
        return MessageType.PREPARE.equals(msgType);
    }

}
