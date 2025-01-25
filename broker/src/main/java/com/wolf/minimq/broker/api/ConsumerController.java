package com.wolf.minimq.broker.api;

import com.wolf.minimq.broker.domain.consumer.Consumer;

public class ConsumerController {
    private final Consumer consumer;

    public ConsumerController(Consumer consumer) {
        this.consumer = consumer;
    }
}
