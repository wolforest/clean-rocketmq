package cn.coderule.minimq.domain.service.broker.consume;

import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;

public interface PopFilter {

    enum FilterResult {
        TO_DLQ,
        NO_MATCH,
        MATCH
    }

    FilterResult filterMessage(RequestContext ctx, String consumerGroup, SubscriptionData subscriptionData,
        MessageBO messageExt);
}
