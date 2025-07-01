package cn.coderule.minimq.domain.service.broker.consume;

import cn.coderule.minimq.domain.core.enums.consume.ConsumerEvent;

public interface ConsumerListener {

    void handle(ConsumerEvent event, String group, Object... args);

    default void shutdown() {
        // resource release operations ...
    }
}
