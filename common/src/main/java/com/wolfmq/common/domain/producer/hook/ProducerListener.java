package com.wolfmq.common.domain.producer.hook;

import com.wolfmq.common.core.event.ProducerEvent;
import com.wolfmq.common.server.broker.ClientChannelInfo;

/**
 * producer manager will call this listener when something happen
 * <p>
 * event type: {@link ProducerEvent}
 */
public interface ProducerListener {

    void handle(ProducerEvent event, String group, ClientChannelInfo clientChannelInfo);

    default void shutdown() {
        // resource release operations ...
    }
}
