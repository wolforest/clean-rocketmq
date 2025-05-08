package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.domain.domain.enums.code.BrokerExceptionCode;
import cn.coderule.minimq.domain.domain.exception.BrokerException;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.cluster.selector.MessageQueueView;
import cn.coderule.minimq.rpc.common.core.RequestContext;

public class QueueSelector {
    private RouteService routeService;

    public MessageQueue select(RequestContext context, MessageBO messageBO) {
        MessageQueueView queueView = routeService.getQueueView(context, messageBO.getTopic());
        if (queueView == null) {
            throw new BrokerException(BrokerExceptionCode.FORBIDDEN, "can not find route of topic");
        }

        return null;
    }
}
