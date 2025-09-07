package cn.coderule.minimq.broker.server.grpc.service.consume;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.consumer.hook.PopFilter;
import java.util.Set;

public class DefaultPopFilter implements PopFilter {
    private final int maxAttempts;

    public DefaultPopFilter(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public FilterResult filterMessage(RequestContext ctx, String consumerGroup, SubscriptionData subscriptionData,
        MessageBO messageExt) {
        if (!isTagMatched(subscriptionData.getTagsSet(), messageExt.getTags())) {
            return FilterResult.NO_MATCH;
        }

        if (messageExt.getReconsumeTimes() >= maxAttempts) {
            return FilterResult.TO_DLQ;
        }

        return FilterResult.MATCH;
    }

    private boolean isTagMatched(Set<String> tagsSet, String tags) {
        if (tagsSet.isEmpty()) {
            return true;
        }

        if (tags == null) {
            return false;
        }

        return tagsSet.contains(tags);
    }
}
