package com.wolfmq.domain.config.base;

import lombok.Getter;

@Getter
public enum RunningMode {
    DEVELOP("Develop"),
    PRODUCTION("Production"),

    MASTER("Master"),
    SLAVE("Slave")
    ;

    private final String name;

    RunningMode(String name) {
        this.name = name;
    }
}
