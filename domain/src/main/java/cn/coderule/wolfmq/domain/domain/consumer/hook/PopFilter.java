package cn.coderule.wolfmq.domain.domain.consumer.hook;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;

public interface PopFilter {

    enum FilterResult {
        TO_DLQ,
        NO_MATCH,
        MATCH
    }

    FilterResult filterMessage(RequestContext ctx, String consumerGroup, SubscriptionData subscriptionData,
        MessageBO messageExt);
}
