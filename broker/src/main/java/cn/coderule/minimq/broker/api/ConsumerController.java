package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.api.validator.GroupValidator;
import cn.coderule.minimq.broker.domain.consumer.consumer.Consumer;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ConsumerInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.consumer.running.ConsumerGroupInfo;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;

public class ConsumerController {
    private final Consumer consumer;

    public ConsumerController(Consumer consumer) {
        this.consumer = consumer;
    }

    public boolean register(RequestContext context, ConsumerInfo consumerInfo) {
        GroupValidator.validate(consumerInfo.getGroupName());
        return consumer.register(consumerInfo);
    }

    public void unregister(RequestContext context, ConsumerInfo consumerInfo) {
        consumer.unregister(consumerInfo);
    }

    public void scanIdleChannels() {
        consumer.scanIdleChannels();
    }

    public ConsumerGroupInfo getGroupInfo(RequestContext context, String groupName) {
        return consumer.getGroupInfo(context, groupName);
    }

    public CompletableFuture<SubscriptionGroup> getSubscription(RequestContext context, String topicName, String groupName) {
        return consumer.getSubscription(context, topicName, groupName);
    }

    public CompletableFuture<PopResult> popMessage(RequestContext context, PopRequest request) {
        return consumer.popMessage(context, request);
    }

    public CompletableFuture<AckResult> ack(RequestContext context, AckRequest request) {
        return consumer.ack(context, request);
    }

    public CompletableFuture<AckResult> changeInvisible(RequestContext context, InvisibleRequest request) {
        return consumer.changeInvisible(context, request);
    }
}
