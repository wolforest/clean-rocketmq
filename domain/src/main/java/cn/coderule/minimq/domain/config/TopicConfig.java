package cn.coderule.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class TopicConfig implements Serializable {
    private boolean enableAutoCreation = true;

    private boolean enableMixedMessageType = false;
    private int defaultQueueNum = 1;
}
