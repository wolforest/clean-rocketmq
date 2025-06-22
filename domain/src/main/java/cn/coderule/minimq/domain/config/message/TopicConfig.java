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
}
