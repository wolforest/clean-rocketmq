package cn.coderule.minimq.domain.service.broker.listener;

import cn.coderule.minimq.domain.domain.enums.ProducerGroupEvent;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;

/**
 * producer manager will call this listener when something happen
 * <p>
 * event type: {@link ProducerGroupEvent}
 */
public interface ProducerListener {

    void handle(ProducerGroupEvent event, String group, ClientChannelInfo clientChannelInfo);
}
