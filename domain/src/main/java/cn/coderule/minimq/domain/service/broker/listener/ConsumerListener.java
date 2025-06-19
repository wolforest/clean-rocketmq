package cn.coderule.minimq.domain.service.broker.listener;

import cn.coderule.minimq.domain.domain.enums.consume.ConsumerEvent;

public interface ConsumerListener {

    void handle(ConsumerEvent event, String group, Object... args);

    default void shutdown() {
        // resource release operations ...
    }
}
