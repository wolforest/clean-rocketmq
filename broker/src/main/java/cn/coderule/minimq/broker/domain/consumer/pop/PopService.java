package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.constant.PermName;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {

    private BrokerConfig brokerConfig;
    private InflightCounter inflightCounter;
    private QueueSelector queueSelector;

    private MQFacade mqFacade;
    private TopicFacade topicFacade;
    private SubscriptionFacade subscriptionFacade;

    public CompletableFuture<PopResult> pop(PopRequest request) {
        MessageQueue messageQueue = queueSelector.select(request);
        PopContext context = new PopContext(request, messageQueue);

        checkConfig(context);

        return null;
    }

    private void checkConfig(PopContext context) {
        checkTopic(context.getPopRequest(), context.getMessageQueue());
        checkSubscriptionGroup(context.getPopRequest());
    }

    private void checkTopic(PopRequest request, MessageQueue messageQueue) {
        Topic topic = topicFacade.getTopic(request.getTopicName());
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
