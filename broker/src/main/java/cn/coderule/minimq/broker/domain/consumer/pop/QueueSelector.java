package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.domain.core.enums.code.BrokerExceptionCode;
import cn.coderule.minimq.domain.core.exception.BrokerException;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.selector.MessageQueueSelector;
import cn.coderule.minimq.domain.domain.cluster.selector.MessageQueueView;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;

public class QueueSelector {
    private final RouteService routeService;

    public QueueSelector(RouteService routeService) {
        this.routeService = routeService;
    }

    public MessageQueue select(PopRequest request) {
        MessageQueueView queueView = routeService.getQueueView(
            request.getRequestContext(),
            request.getTopicName()
        );

        if (queueView == null) {
            throw new BrokerException(
                BrokerExceptionCode.FORBIDDEN, "can not find route of topic"
            );
        }

        MessageQueue queue = null;
        MessageQueueSelector selector = queueView.getReadSelector();
        if (StringUtil.notBlank(request.getStoreGroup())) {
            queue = selector.getQueueByBrokerName(request.getStoreGroup());
        } else {
            queue = selector.selectOne(true);
        }

        if (queue == null) {
            throw new BrokerException(
                BrokerExceptionCode.FORBIDDEN, "No readable queue"
            );
        }

        return queue;
    }
}
