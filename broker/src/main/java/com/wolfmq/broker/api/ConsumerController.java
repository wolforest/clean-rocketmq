package com.wolfmq.broker.api;

import com.wolfmq.broker.api.validator.GroupValidator;
import com.wolfmq.broker.api.validator.ConsumeValidator;
import com.wolfmq.broker.domain.consumer.Consumer;
import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.ack.broker.AckRequest;
import com.wolfmq.domain.domain.consumer.ConsumerInfo;
import com.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import com.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import com.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import com.wolfmq.domain.domain.consumer.consume.pop.PopResult;
import com.wolfmq.domain.domain.consumer.running.ConsumerGroupInfo;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;

public class ConsumerController {
    private final MessageConfig messageConfig;

    private final Consumer consumer;

    private final ConsumeValidator consumeValidator;

    public ConsumerController(BrokerConfig brokerConfig, Consumer consumer) {
        this.messageConfig = brokerConfig.getMessage();

        this.consumer = consumer;
        this.consumeValidator = new ConsumeValidator(brokerConfig);

    }

    public boolean register(ConsumerInfo consumerInfo) {
        GroupValidator.validate(consumerInfo.getGroupName());
        return consumer.register(consumerInfo);
    }

    public void unregister(ConsumerInfo consumerInfo) {
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

    public CompletableFuture<PopResult> popMessage(PopRequest request) {
        formatInvisibleTime(request);
        consumeValidator.validate(request);
        return consumer.popMessage(request);
    }

    public CompletableFuture<AckResult> ack(AckRequest request) {
        consumeValidator.validate(request);
        return consumer.ack(request);
    }

    public CompletableFuture<AckResult> changeInvisible(InvisibleRequest request) {
        consumeValidator.validate(request);
        return consumer.changeInvisible(request);
    }

    private void formatInvisibleTime(PopRequest request) {
        if (messageConfig.isEnableAutoRenew() && request.isAutoRenew()) {
            request.setInvisibleTime(messageConfig.getDefaultInvisibleTime());
        } else {
            consumeValidator.validateInvisibleTime(request.getInvisibleTime());
        }
    }
}
