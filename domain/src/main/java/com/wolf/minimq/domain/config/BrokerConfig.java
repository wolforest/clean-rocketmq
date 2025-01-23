package com.wolf.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class BrokerConfig implements Serializable {
    private boolean enableTrace = false;
    private boolean enableTopicAutoCreation = true;
}
