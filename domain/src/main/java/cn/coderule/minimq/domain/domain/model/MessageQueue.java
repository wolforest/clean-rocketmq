package cn.coderule.minimq.domain.domain.model;

import cn.coderule.common.util.net.Address;
import cn.coderule.minimq.domain.domain.enums.MessageType;
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
    private String cluster;
    private String serverGroup;
    private Address serverAddress;
    private int serverId;

    private String namespace;
    private String topic;

    private int queueId;
    private int permission;
    private MessageType messageType;

    @Override
    public int compareTo(MessageQueue o) {
        int topicDiff = this.topic.compareTo(o.topic);
        if (topicDiff != 0) {
            return topicDiff;
        }

        int brokerDiff = this.serverGroup.compareTo(o.serverGroup);
        if (brokerDiff != 0) {
            return brokerDiff;
        }

        int brokerIdDiff = this.serverId - o.serverId;
        if (brokerIdDiff != 0) {
            return brokerIdDiff;
        }

        return this.queueId - o.queueId;
    }
}
