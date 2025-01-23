package com.wolf.minimq.domain.config;

import com.wolf.common.util.lang.SystemUtil;
import java.io.Serializable;
import lombok.Data;

@Data
public class BrokerConfig implements Serializable {
    private boolean enableTrace = false;
    private boolean enableTopicAutoCreation = true;

    private int producerThreadNum = SystemUtil.getProcessorNumber();
    private int producerQueueCapacity = 10000;
    private int consumerThreadNum = SystemUtil.getProcessorNumber();
    private int consumerQueueCapacity = 10000;
}
