package com.wolfmq.domain.domain.consumer.hook;

import com.wolfmq.domain.core.event.ConsumerEvent;

public interface ConsumerListener {

    void handle(ConsumerEvent event, String group, Object... args);

    default void shutdown() {
        // resource release operations ...
    }
}
