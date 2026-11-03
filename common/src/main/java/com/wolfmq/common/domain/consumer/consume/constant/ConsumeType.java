package com.wolfmq.common.domain.consumer.consume.constant;

import lombok.Getter;

@Getter
public enum ConsumeType {

    CONSUME_ACTIVELY("PULL"),

    CONSUME_PASSIVELY("PUSH"),

    CONSUME_POP("POP");

    private final String typeCN;

    ConsumeType(String typeCN) {
        this.typeCN = typeCN;
    }

}
