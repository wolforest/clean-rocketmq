package cn.coderule.minimq.domain.service.broker.listener;

import cn.coderule.minimq.domain.domain.enums.produce.ProducerEvent;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;

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
