package com.wolfmq.broker.api.validator;

import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.core.constant.code.InvalidCode;
import com.wolfmq.domain.core.exception.InvalidRequestException;
import com.wolfmq.domain.domain.consumer.ack.broker.AckRequest;
import com.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import com.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import com.wolfmq.domain.domain.meta.topic.TopicValidator;

public class ConsumeValidator {
    private final MessageConfig messageConfig;

    public ConsumeValidator(BrokerConfig brokerConfig) {
        this.messageConfig = brokerConfig.getMessage();
    }

    public void validate(PopRequest request) {
        GroupValidator.validate(request.getConsumerGroup());
        TopicValidator.validateTopic(request.getTopicName());
        validateMaxNum(request);
    }

    public void validate(InvisibleRequest request) {
        GroupValidator.validate(request.getGroupName());
        TopicValidator.validateTopic(request.getTopicName());
        validateInvisibleTime(request.getInvisibleTime());
    }

    public void validate(AckRequest request) {
        GroupValidator.validate(request.getGroupName());
        TopicValidator.validateTopic(request.getTopicName());
    }

    public void validateInvisibleTime(long invisibleTime) {
        if (invisibleTime < messageConfig.getMinInvisibleTime()) {
            throw new InvalidRequestException(
                InvalidCode.ILLEGAL_INVISIBLE_TIME,
                "Invisible time is smaller than: " + messageConfig.getMinInvisibleTime());
        }

        long maxInvisibleTime = messageConfig.getMaxInvisibleTime();
        if (maxInvisibleTime <= 0) {
            return;
        }

        if (invisibleTime > maxInvisibleTime) {
            throw new InvalidRequestException(
                InvalidCode.ILLEGAL_INVISIBLE_TIME,
                "Invisible time is larger than: " + messageConfig.getMaxInvisibleTime());
        }
    }

    private void validateMaxNum(PopRequest request) {
        if (request.getMaxNum() <= messageConfig.getMaxPopSize()) {
            return;
        }
        throw new InvalidRequestException(
            InvalidCode.BAD_REQUEST,
            "Max pop num is larger than: " + messageConfig.getMaxPopSize());
    }


}
