package cn.coderule.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class TopicConfig implements Serializable {
    private boolean enableAutoCreation = true;

    private int defaultQueueNum = 1;
}
