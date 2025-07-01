package cn.coderule.minimq.domain.config.message;

import java.io.Serializable;
import lombok.Data;

@Data
public class TopicConfig implements Serializable {
    // do not change this value
    private int maxTopicLength = 127;

    private boolean enableAutoCreation = true;

    private boolean enableMixedMessageType = false;
    private int defaultQueueNum = 1;

    private int timerQueueNum = 1;
    private int transactionQueueNum = 1;
    private int reviveQueueNum = 8;

    // read message from pop retry topic v1, for the compatibility, will be removed in the future version
    private boolean retrieveMessageFromPopRetryTopicV1 = true;
    private boolean enableRetryTopicV2 = false;
}
