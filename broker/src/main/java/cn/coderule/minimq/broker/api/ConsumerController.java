package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.consumer.Consumer;

public class ConsumerController {
    private final Consumer consumer;

    public ConsumerController(Consumer consumer) {
        this.consumer = consumer;
    }
}
