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

        MessageQueueSelector selector = queueView.getReadSelector();
        if (StringUtil.notBlank(request.getStoreGroup())) {
            return selector.getQueueByBrokerName(request.getStoreGroup());
        }

        return selector.selectOne(true);
    }
}
