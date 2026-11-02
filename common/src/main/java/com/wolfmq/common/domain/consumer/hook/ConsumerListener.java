package com.wolfmq.common.domain.consumer.hook;

import com.wolfmq.common.core.event.ConsumerEvent;

public interface ConsumerListener {

    void handle(ConsumerEvent event, String group, Object... args);

    default void shutdown() {
        // resource release operations ...
    }
}
