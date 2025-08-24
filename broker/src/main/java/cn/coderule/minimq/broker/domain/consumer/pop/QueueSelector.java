package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.code.BrokerExceptionCode;
import cn.coderule.minimq.domain.core.exception.BrokerException;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.selector.MessageQueueSelector;
import cn.coderule.minimq.domain.domain.cluster.selector.MessageQueueView;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import java.util.concurrent.atomic.AtomicLong;

public class QueueSelector {
    private final BrokerConfig brokerConfig;
    private final RouteService routeService;

    private final AtomicLong reviveCount = new AtomicLong(0);

    public QueueSelector(BrokerConfig brokerConfig, RouteService routeService) {
        this.brokerConfig = brokerConfig;
        this.routeService = routeService;
    }

    public void select(PopContext context) {
        PopRequest request = context.getRequest();
        MessageQueue messageQueue = selectQueue(request);
        context.setMessageQueue(messageQueue);

        selectReviveQueue(context);
    }

    private MessageQueue selectQueue(PopRequest request) {
        MessageQueueView queueView = routeService.getQueueView(
            request.getRequestContext(),
            request.getTopicName()
        );

        if (queueView == null) {
            throw new BrokerException(
                BrokerExceptionCode.FORBIDDEN, "can not find route of topic"
            );
        }

        MessageQueue queue;
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

    private void selectReviveQueue(PopContext context) {
        if (context.getRequest().isFifo()) {
            context.setReviveQueueId(KeyBuilder.POP_ORDER_REVIVE_QUEUE);
            return;
        }

        int queueNum = brokerConfig.getTopicConfig().getReviveQueueNum();
        int queueId = (int) Math.abs(reviveCount.getAndIncrement() % queueNum);
        context.setReviveQueueId(queueId);
    }
}
