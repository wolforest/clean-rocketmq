package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.domain.config.business.TopicConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.constant.PermName;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.rpc.broker.core.FilterAPI;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {

    private BrokerConfig brokerConfig;
    private InflightCounter inflightCounter;
    private QueueSelector queueSelector;
    private ConsumerRegister consumerRegister;

    private MQFacade mqFacade;
    private TopicFacade topicFacade;
    private SubscriptionFacade subscriptionFacade;

    private final AtomicLong reviveCount = new AtomicLong(0);

    public CompletableFuture<PopResult> pop(PopRequest request) {
        MessageQueue messageQueue = queueSelector.select(request);
        PopContext context = new PopContext(request, messageQueue);

        checkConfig(context);
        compensateSubscription(context);
        selectReviveQueue(context);

        return null;
    }

    private void selectReviveQueue(PopContext context) {
        if (context.getPopRequest().isFifo()) {
            context.setReviveQueueId(KeyBuilder.POP_ORDER_REVIVE_QUEUE);
            return;
        }

        int queueNum = brokerConfig.getTopicConfig().getReviveQueueNum();
        int queueId = (int) Math.abs(reviveCount.getAndIncrement() % queueNum);
        context.setReviveQueueId(queueId);
    }

    private void  compensateSubscription(PopContext context) {
        PopRequest request = context.getPopRequest();
        consumerRegister.compensateSubscription(
            request.getConsumerGroup(),
            request.getTopicName(),
            request.getSubscriptionData()
        );

        compensateRetrySubscription(request);
    }

    private void  compensateRetrySubscription(PopRequest request) {
        String retryTopic = KeyBuilder.buildPopRetryTopic(
            request.getTopicName(), request.getConsumerGroup()
        );

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

    private void checkConfig(PopContext context) {
        checkTopic(context);
        checkSubscriptionGroup(context.getPopRequest());
    }

    private void checkTopic(PopContext context) {
        PopRequest request = context.getPopRequest();
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

    private void checkSubscriptionGroup(PopRequest request) {
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

}
