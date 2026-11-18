package com.wolfmq.common.domain.meta.exchange;

import java.io.Serializable;
import lombok.Data;

@Data
public class ExchangeRule implements Serializable {
    private ExchangeType type = ExchangeType.FANOUT;

    private String name;
    private String description;

    private String pattern;
    private String queue;
}
