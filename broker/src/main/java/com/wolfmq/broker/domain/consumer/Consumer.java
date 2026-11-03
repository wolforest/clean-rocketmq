package com.wolfmq.broker.domain.consumer;

import com.wolfmq.broker.domain.consumer.ack.BrokerAckService;
import com.wolfmq.broker.domain.consumer.ack.InvisibleService;
import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.broker.domain.consumer.pop.PopService;
import com.wolfmq.common.domain.consumer.ack.broker.AckResult;
import com.wolfmq.common.domain.consumer.consume.pop.PopResult;
import com.wolfmq.common.domain.consumer.ConsumerInfo;
import com.wolfmq.common.domain.consumer.ack.broker.AckRequest;
import com.wolfmq.common.domain.consumer.ack.broker.InvisibleRequest;
import com.wolfmq.common.domain.consumer.consume.pop.PopRequest;
import com.wolfmq.common.domain.consumer.running.ConsumerGroupInfo;
import com.wolfmq.common.server.broker.RequestContext;
import com.wolfmq.common.domain.meta.subscription.SubscriptionGroup;
import com.wolfmq.rpc.store.facade.SubscriptionFacade;
import java.util.concurrent.CompletableFuture;

/**
 * gateway of package consumer
 *  - support ConsumerController
 *  - support other packages
 */
public class Consumer  {
    private final PopService popService;
    private final BrokerAckService ackService;
    private final ConsumerManager register;
    private final InvisibleService invisibleService;
    private final SubscriptionFacade subscriptionStore;

    public Consumer(
        PopService popService,
        BrokerAckService ackService,
        ConsumerManager register,
        InvisibleService invisibleService,
        SubscriptionFacade subscriptionStore
    ) {
        this.popService = popService;
        this.ackService = ackService;
        this.register = register;
        this.invisibleService = invisibleService;
        this.subscriptionStore = subscriptionStore;
    }

    public boolean register(ConsumerInfo consumerInfo) {
        return register.register(consumerInfo);
    }

    public void unregister(ConsumerInfo consumerInfo) {
        register.unregister(consumerInfo);
    }

    public void scanIdleChannels() {
        register.scanIdleChannels();
    }

    public ConsumerGroupInfo getGroupInfo(RequestContext context, String groupName) {
        return register.getGroupInfo(groupName);
    }

    public CompletableFuture<SubscriptionGroup> getSubscription(RequestContext context, String topicName, String groupName) {
        return subscriptionStore.getGroupAsync(topicName, groupName);
    }

    public CompletableFuture<PopResult> popMessage(PopRequest request) {
        return popService.pop(request);
    }

    public CompletableFuture<AckResult> ack(AckRequest request) {
        return ackService.ack(request);
    }

    public CompletableFuture<AckResult> changeInvisible(InvisibleRequest request) {
        return invisibleService.changeInvisible(request);
    }

}
