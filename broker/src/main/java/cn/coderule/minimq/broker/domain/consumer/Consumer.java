package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.broker.domain.consumer.ack.AckService;
import cn.coderule.minimq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.domain.consumer.pop.PopService;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.consumer.ConsumerInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.running.ConsumerGroupInfo;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.rpc.store.facade.SubscriptionFacade;
import java.util.concurrent.CompletableFuture;

/**
 * gateway of package consumer
 *  - support ConsumerController
 *  - support other packages
 */
public class Consumer  {
    private final PopService popService;
    private final AckService ackService;
    private final ConsumerRegister register;
    private final InvisibleService invisibleService;
    private final SubscriptionFacade subscriptionStore;

    public Consumer(
        PopService popService,
        AckService ackService,
        ConsumerRegister register,
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

    public CompletableFuture<AckResult> ack(RequestContext context, AckRequest request) {
        return ackService.ack(context, request);
    }

    public CompletableFuture<AckResult> changeInvisible(InvisibleRequest request) {
        return invisibleService.changeInvisible(request.getRequestContext(), request);
    }

}
