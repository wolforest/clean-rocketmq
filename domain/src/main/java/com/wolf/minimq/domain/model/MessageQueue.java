package com.wolf.minimq.domain.model;

import com.wolf.minimq.domain.enums.MessageType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageQueue implements Comparable<MessageQueue>, Serializable {
    private String broker;
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

        int brokerDiff = this.broker.compareTo(o.broker);
        if (brokerDiff != 0) {
            return brokerDiff;
        }

        return this.queueId - o.queueId;
    }
}
