package com.wolfmq.common.domain.consumer.hook;

import com.wolfmq.common.server.broker.RequestContext;
import com.wolfmq.common.server.registry.heartbeat.SubscriptionData;
import com.wolfmq.common.domain.message.MessageBO;

public interface PopFilter {

    enum FilterResult {
        TO_DLQ,
        NO_MATCH,
        MATCH
    }

    FilterResult filterMessage(RequestContext ctx, String consumerGroup, SubscriptionData subscriptionData,
        MessageBO messageExt);
}
