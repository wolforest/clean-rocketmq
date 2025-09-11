package cn.coderule.minimq.domain.domain.consumer.ack;

import com.alibaba.fastjson2.annotation.JSONField;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckInfo implements Serializable {

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
