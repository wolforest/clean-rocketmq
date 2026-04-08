package cn.coderule.wolfmq.domain.domain.consumer.hook;

import cn.coderule.wolfmq.domain.core.enums.consume.ConsumerEvent;

public interface ConsumerListener {

    void handle(ConsumerEvent event, String group, Object... args);

    default void shutdown() {
        // resource release operations ...
    }
}
