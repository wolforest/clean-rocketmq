package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.constant.PermName;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.rpc.broker.core.FilterAPI;
import cn.coderule.minimq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopContextBuilder {
    private final BrokerConfig brokerConfig;
    private final ConsumerRegister consumerRegister;

    private final TopicFacade topicFacade;
    private final SubscriptionFacade subscriptionFacade;

    public PopContextBuilder(
        BrokerConfig brokerConfig,
        ConsumerRegister consumerRegister,
        TopicFacade topicFacade,
        SubscriptionFacade subscriptionFacade
    ) {
        this.brokerConfig = brokerConfig;
        this.consumerRegister = consumerRegister;
        this.topicFacade = topicFacade;
        this.subscriptionFacade = subscriptionFacade;
    }

    public PopContext create(PopRequest request) {
        PopContext context = new PopContext(brokerConfig, request);

        loadTopic(context);
        loadRetryTopic(context);
        loadSubscriptionGroup(context.getRequest());

        compensateSubscription(context);
        compensateRetrySubscription(context);

        return context;
    }

    private void loadTopic(PopContext context) {
        PopRequest request = context.getRequest();
        MessageQueue messageQueue = context.getMessageQueue();
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

        if (messageQueue.getQueueId() >= topic.getReadQueueNums()) {
            log.error("Topic queueId error: topic={}, queueId={}",
                request.getTopicName(), messageQueue.getQueueId());
            throw new InvalidRequestException(
                InvalidCode.ILLEGAL_TOPIC,
                "Message queue can not find: " + request.getTopicName()
                    + ":" + messageQueue.getQueueId()
            );
        }
    }

    private void loadRetryTopic(PopContext context) {
        if (!context.shouldRetry()) {
            return;
        }

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
        consumerRegister.compensateSubscription(
            request.getConsumerGroup(),
            request.getTopicName(),
            request.getSubscriptionData()
        );
    }

    private void  compensateRetrySubscription(PopContext context) {
        PopRequest request = context.getRequest();
        String retryTopic = context.getRetryTopic().getTopicName();

        try {
            SubscriptionData retrySubscription = FilterAPI.build(
                retryTopic,
                "*",
                request.getSubscriptionData().getExpressionType()
            );

            consumerRegister.compensateSubscription(
                request.getConsumerGroup(),
                retryTopic,
                retrySubscription
            );
        } catch (Exception e) {
            log.warn("build retry subscription failed", e);
        }
    }

}
