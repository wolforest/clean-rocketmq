package cn.coderule.minimq.domain.domain.model;

import cn.coderule.minimq.domain.domain.enums.message.MessageType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * message queue
 *  - route for publisher
 *  - assignment for consumer
 *  - key info register to name server
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageQueue implements Comparable<MessageQueue>, Serializable {
    private String clusterName;
    private String groupName;
    private int groupNo;
    private String namespace;
    private String topicName;
    private int queueId;

    private String address;
    private int permission;
    private MessageType messageType;

    public MessageQueue(String topicName, String groupName, int queueId) {
        this.topicName = topicName;
        this.groupName = groupName;
        this.queueId = queueId;
    }

    @Override
    public int compareTo(MessageQueue o) {
        int topicDiff = this.topicName.compareTo(o.topicName);
        if (topicDiff != 0) {
            return topicDiff;
        }

        int brokerDiff = this.groupName.compareTo(o.groupName);
        if (brokerDiff != 0) {
            return brokerDiff;
        }

        return this.queueId - o.queueId;
    }
}
