package com.wolfmq.common.domain.meta.exchange;

import java.io.Serializable;
import lombok.Data;

@Data
public class ExchangeRule implements Serializable {
    private ExchangeType type = ExchangeType.FANOUT;

    /**
     * exchange work within topic
     */
    private String topic;
    /**
     * name should be unique in certain topic
     */
    private String name;
    /**
     * description is optional
     */
    private String description;

    private String pattern;
    private String queue;
}
