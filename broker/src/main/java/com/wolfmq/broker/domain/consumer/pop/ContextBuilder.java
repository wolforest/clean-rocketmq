package com.wolfmq.broker.domain.consumer.pop;

import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.meta.topic.PermName;
import com.wolfmq.domain.core.constant.code.InvalidCode;
import com.wolfmq.domain.core.exception.InvalidRequestException;
import com.wolfmq.domain.server.ClientChannelInfo;
import com.wolfmq.domain.server.heartbeat.SubscriptionData;
import com.wolfmq.domain.domain.consumer.consume.pop.PopContext;
import com.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import com.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import com.wolfmq.domain.domain.meta.topic.KeyBuilder;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.rpc.broker.core.FilterAPI;
import com.wolfmq.rpc.store.facade.SubscriptionFacade;
import com.wolfmq.rpc.store.facade.TopicFacade;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextBuilder {
    private final BrokerConfig brokerConfig;
    private final ConsumerManager consumerManager;

    private final TopicFacade topicFacade;
    private final SubscriptionFacade subscriptionFacade;

    public ContextBuilder(
        BrokerConfig brokerConfig,
        ConsumerManager consumerManager,
        TopicFacade topicFacade,
        SubscriptionFacade subscriptionFacade
    ) {
        this.brokerConfig = brokerConfig;
        this.consumerManager = consumerManager;
        this.topicFacade = topicFacade;
        this.subscriptionFacade = subscriptionFacade;
    }

    public PopContext build(PopRequest request) {
        PopContext context = new PopContext(brokerConfig, request);

        loadTopic(context);
        loadRetryTopic(context);
        loadSubscriptionGroup(context.getRequest());

        compensateSubscription(context);
        compensateRetrySubscription(context);

        findChannel(context);

        return context;
    }

    private void loadTopic(PopContext context) {
        PopRequest request = context.getRequest();
        Topic topic = topicFacade.getTopic(request.getTopicName());
        context.setTopic(topic);

        if (topic == null) {
            log.error("Topic not exists: {}", request.getTopicName());
            throw new InvalidRequestException(
                InvalidCode.ILLEGAL_TOPIC, "Topic not exists: " + request.getTopicName());
        }

        if (!PermName.isReadable(topic.getPerm())) {
            log.error("Topic permission error: {}", request.getTopicName());
            throw new InvalidRequestException(
                InvalidCode.ILLEGAL_TOPIC, "Topic permission error: " + request.getTopicName());
        }
    }

    private void loadRetryTopic(PopContext context) {
        PopRequest request = context.getRequest();
        String retryTopicName = KeyBuilder.buildPopRetryTopic(
            request.getTopicName(),
            request.getConsumerGroup()
        );

        Topic retryTopic = topicFacade.getTopic(retryTopicName);
        context.setRetryTopic(retryTopic);
    }

    private void loadSubscriptionGroup(PopRequest request) {
        SubscriptionGroup subscriptionGroup = subscriptionFacade.getGroup(
            request.getTopicName(), request.getConsumerGroup());
        if (subscriptionGroup == null) {
            log.error("Consumer group error: topic={}, consumerGroup={}",
                request.getTopicName(), request.getConsumerGroup());
            throw new InvalidRequestException(
                InvalidCode.ILLEGAL_CONSUMER_GROUP,
                "Consumer group not exist: " + request.getConsumerGroup()
            );
        }

        if (!subscriptionGroup.isConsumeEnable()) {
            log.error("Consumer group is not consumable: topic={}, consumerGroup={}",
                request.getTopicName(), request.getConsumerGroup());
            throw new InvalidRequestException(
                InvalidCode.ILLEGAL_CONSUMER_GROUP,
                "Consumer group is not consumable: " + request.getTopicName()
                    + ":" + request.getConsumerGroup()
            );
        }
    }

    private void  compensateSubscription(PopContext context) {
        PopRequest request = context.getRequest();
        consumerManager.compensateSubscription(
            request.getConsumerGroup(),
            request.getTopicName(),
            request.getSubscriptionData()
        );
    }

    private void  compensateRetrySubscription(PopContext context) {
        if (!context.shouldRetry()) {
            return;
        }

        PopRequest request = context.getRequest();
        String retryTopic = context.getRetryTopic().getTopicName();

        try {
            SubscriptionData retrySubscription = FilterAPI.build(
                retryTopic,
                "*",
                request.getSubscriptionData().getExpressionType()
            );

            consumerManager.compensateSubscription(
                request.getConsumerGroup(),
                retryTopic,
                retrySubscription
            );
        } catch (Exception e) {
            log.warn("build retry subscription failed", e);
        }
    }

    private void findChannel(PopContext context) {
        ClientChannelInfo channelInfo = consumerManager.findChannel(
            context.getRequest().getConsumerGroup(),
            context.getRequest().getRequestContext().getClientID()
        );

        if (channelInfo == null) {
            context.setChannel(null);
            return;
        }

        Channel channel = channelInfo.getChannel();
        context.setChannel(channel);
    }

}
