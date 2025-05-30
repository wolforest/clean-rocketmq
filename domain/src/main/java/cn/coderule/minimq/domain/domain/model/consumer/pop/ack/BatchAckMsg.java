package cn.coderule.minimq.domain.domain.model.consumer.pop.ack;

import com.alibaba.fastjson2.annotation.JSONField;
import java.util.ArrayList;
import java.util.List;


public class BatchAckMsg extends AckMsg {
    @JSONField(name = "aol", alternateNames = {"ackOffsetList"})
    private List<Long> ackOffsetList = new ArrayList<>(32);


    public List<Long> getAckOffsetList() {
        return ackOffsetList;
    }

    public void setAckOffsetList(List<Long> ackOffsetList) {
        this.ackOffsetList = ackOffsetList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BatchAckMsg{");
        sb.append("ackOffsetList=").append(ackOffsetList);
        sb.append(", startOffset=").append(getStartOffset());
        sb.append(", consumerGroup='").append(getConsumerGroup()).append('\'');
        sb.append(", topic='").append(getTopic()).append('\'');
        sb.append(", queueId=").append(getQueueId());
        sb.append(", popTime=").append(getPopTime());
        sb.append(", brokerName=").append(getBrokerName());
        sb.append('}');
        return sb.toString();
    }
}
