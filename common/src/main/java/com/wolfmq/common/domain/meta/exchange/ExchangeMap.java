package com.wolfmq.common.domain.meta.exchange;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

@Data
public class ExchangeMap implements Serializable {
    // topic -> exchangeName -> exchangeRule
    private Map<String, Map<String, ExchangeRule>> tree;
}
