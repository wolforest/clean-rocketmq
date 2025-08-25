package cn.coderule.minimq.domain.core.enums.consume;

import lombok.Getter;

@Getter
public enum ConsumeStrategy {
    CONSUME_FROM_START(10),
    CONSUME_FROM_LATEST(20),
    CONSUME_FROM_LAST_OFFSET(30),

    CONSUME_FROM_FIRST_OFFSET(40),
    CONSUME_FROM_TIMESTAMP(50),
    ;

    private final int code;

    ConsumeStrategy(int code) {
        this.code = code;
    }
}
