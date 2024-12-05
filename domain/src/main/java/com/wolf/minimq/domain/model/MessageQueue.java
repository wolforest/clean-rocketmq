package com.wolf.minimq.domain.model;

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
    private String topic;
    private String brokerName;
    private int queueId;

    @Override
    public int compareTo(MessageQueue o) {
        int topicDiff = this.topic.compareTo(o.topic);
        if (topicDiff != 0) {
            return topicDiff;
        }

        int brokerDiff = this.brokerName.compareTo(o.brokerName);
        if (brokerDiff != 0) {
            return brokerDiff;
        }

        return this.queueId - o.queueId;
    }
}
