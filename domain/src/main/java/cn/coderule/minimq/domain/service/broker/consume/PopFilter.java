package cn.coderule.minimq.domain.service.broker.consume;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.domain.message.MessageBO;

public interface PopFilter {

    enum FilterResult {
        TO_DLQ,
        NO_MATCH,
        MATCH
    }

    FilterResult filterMessage(RequestContext ctx, String consumerGroup, SubscriptionData subscriptionData,
        MessageBO messageExt);
}
