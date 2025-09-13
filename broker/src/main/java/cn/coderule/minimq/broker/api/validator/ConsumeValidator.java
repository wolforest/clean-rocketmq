package cn.coderule.minimq.broker.api.validator;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;

public class ConsumeValidator {
    private final MessageConfig messageConfig;

    public ConsumeValidator(BrokerConfig brokerConfig) {
        this.messageConfig = brokerConfig.getMessageConfig();
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
