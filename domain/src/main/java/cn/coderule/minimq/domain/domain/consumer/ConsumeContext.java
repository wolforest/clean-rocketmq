package cn.coderule.minimq.domain.domain.consumer;

import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import java.util.Map;

public class ConsumeContext {
    private String consumerGroup;
    private String topicName;
    private Integer queueId;
    private String clientHost;
    private String storeHost;
    private Map<String, Long> messageIds;
    private int bodyLength;
    private boolean success;
    private String status;
    private Object mqTraceContext;
    private Topic topic;

    private String accountAuthType;
    private String accountOwnerParent;
    private String accountOwnerSelf;
    private int rcvMsgNum;
    private int rcvMsgSize;


    private String namespace;
    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public String getStoreHost() {
        return storeHost;
    }

    public void setStoreHost(String storeHost) {
        this.storeHost = storeHost;
    }

    public Map<String, Long> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(Map<String, Long> messageIds) {
        this.messageIds = messageIds;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getMqTraceContext() {
        return mqTraceContext;
    }

    public void setMqTraceContext(Object mqTraceContext) {
        this.mqTraceContext = mqTraceContext;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public String getAccountAuthType() {
        return accountAuthType;
    }

    public void setAccountAuthType(String accountAuthType) {
        this.accountAuthType = accountAuthType;
    }

    public String getAccountOwnerParent() {
        return accountOwnerParent;
    }

    public void setAccountOwnerParent(String accountOwnerParent) {
        this.accountOwnerParent = accountOwnerParent;
    }

    public String getAccountOwnerSelf() {
        return accountOwnerSelf;
    }

    public void setAccountOwnerSelf(String accountOwnerSelf) {
        this.accountOwnerSelf = accountOwnerSelf;
    }

    public int getRcvMsgNum() {
        return rcvMsgNum;
    }

    public void setRcvMsgNum(int rcvMsgNum) {
        this.rcvMsgNum = rcvMsgNum;
    }

    public int getRcvMsgSize() {
        return rcvMsgSize;
    }

    public void setRcvMsgSize(int rcvMsgSize) {
        this.rcvMsgSize = rcvMsgSize;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
