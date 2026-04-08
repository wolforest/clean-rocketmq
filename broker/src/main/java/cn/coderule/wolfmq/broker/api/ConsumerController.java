package cn.coderule.wolfmq.broker.api;

import cn.coderule.wolfmq.broker.api.validator.GroupValidator;
import cn.coderule.wolfmq.broker.api.validator.ConsumeValidator;
import cn.coderule.wolfmq.broker.domain.consumer.Consumer;
import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.wolfmq.domain.domain.consumer.ConsumerInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.wolfmq.domain.domain.consumer.running.ConsumerGroupInfo;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;

public class ConsumerController {
    private final MessageConfig messageConfig;

    private final Consumer consumer;

    private final ConsumeValidator consumeValidator;

    public ConsumerController(BrokerConfig brokerConfig, Consumer consumer) {
        this.messageConfig = brokerConfig.getMessageConfig();

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
