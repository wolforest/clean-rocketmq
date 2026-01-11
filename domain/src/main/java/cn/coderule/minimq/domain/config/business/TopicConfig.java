package cn.coderule.minimq.domain.config.business;

import java.io.Serializable;
import lombok.Data;

@Data
public class TopicConfig implements Serializable {
    // do not change this value
    private int maxTopicLength = 127;

    private boolean enableAutoCreation = true;

    /**
     * enable wal topic(kafka like topic)
     */
    private boolean enableWalTopic = false;
    private int maxWalTopicNum = 10;

    private boolean enableMixedMessageType = false;
    private int defaultQueueNum = 1;

    private int timerQueueNum = 1;
    private int reviveQueueNum = 1;
    // 1 for test
    //private int reviveQueueNum = 8;
    private int prepareQueueNum = 1;
    private int commitQueueNum = 1;
    private int checkQueueNum = 1;
    private int discardQueueNum = 1;

    // read message from pop retry topic v1, for the compatibility, will be removed in the future version
    private boolean retrieveMessageFromPopRetryTopicV1 = true;
    private boolean enableRetryTopicV2 = false;
}
