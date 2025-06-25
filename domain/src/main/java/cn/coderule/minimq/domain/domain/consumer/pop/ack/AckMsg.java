package cn.coderule.minimq.domain.domain.model.consumer.pop.ack;

import com.alibaba.fastjson2.annotation.JSONField;

public class AckMsg {

    @JSONField(name = "ao", alternateNames = {"ackOffset"})
    private long ackOffset;

    @JSONField(name = "so", alternateNames = {"startOffset"})
    private long startOffset;

    @JSONField(name = "c", alternateNames = {"consumerGroup"})
    private String consumerGroup;

    @JSONField(name = "t", alternateNames = {"topic"})
    private String topic;

    @JSONField(name = "q", alternateNames = {"queueId"})
    private int queueId;

    @JSONField(name = "pt", alternateNames = {"popTime"})
    private long popTime;

    @JSONField(name = "bn", alternateNames = {"brokerName"})
    private String brokerName;

    public long getPopTime() {
        return popTime;
    }

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public long getAckOffset() {
        return ackOffset;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public void setAckOffset(long ackOffset) {
        this.ackOffset = ackOffset;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AckMsg{");
        sb.append("ackOffset=").append(ackOffset);
        sb.append(", startOffset=").append(startOffset);
        sb.append(", consumerGroup='").append(consumerGroup).append('\'');
        sb.append(", topic='").append(topic).append('\'');
        sb.append(", queueId=").append(queueId);
        sb.append(", popTime=").append(popTime);
        sb.append(", brokerName=").append(brokerName);
        sb.append('}');
        return sb.toString();
    }
}
