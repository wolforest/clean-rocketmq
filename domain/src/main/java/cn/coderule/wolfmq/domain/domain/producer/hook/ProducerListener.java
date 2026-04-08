package cn.coderule.wolfmq.domain.domain.producer.hook;

import cn.coderule.wolfmq.domain.core.enums.produce.ProducerEvent;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;

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
