package cn.coderule.minimq.domain.service.broker.listener;

import cn.coderule.minimq.domain.domain.enums.consume.ConsumerGroupEvent;

public interface ConsumerListener {

    void handle(ConsumerGroupEvent event, String group, Object... args);

    void shutdown();
}
