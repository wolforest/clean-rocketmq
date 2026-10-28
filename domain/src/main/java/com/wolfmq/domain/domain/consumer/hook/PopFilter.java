package com.wolfmq.domain.domain.consumer.hook;

import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.server.heartbeat.SubscriptionData;
import com.wolfmq.domain.domain.message.MessageBO;

public interface PopFilter {

    enum FilterResult {
        TO_DLQ,
        NO_MATCH,
        MATCH
    }

    FilterResult filterMessage(RequestContext ctx, String consumerGroup, SubscriptionData subscriptionData,
        MessageBO messageExt);
}
