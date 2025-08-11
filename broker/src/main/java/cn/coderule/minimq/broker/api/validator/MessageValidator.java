package cn.coderule.minimq.broker.api.validator;

import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;

import static cn.coderule.common.util.lang.string.StringUtil.containControlCharacter;

public class MessageValidator {
    private final BrokerConfig brokerConfig;
    private final MessageConfig messageConfig;

    public MessageValidator(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.messageConfig = brokerConfig.getMessageConfig();
    }

    public void validate(MessageBO messageBO) {
        validateTopic(messageBO.getTopic());
        validateTag(messageBO.getTags());

        validateDeliverTime(messageBO.getDeliverTime());
        validateShardingKey(messageBO.getShardingKey());
        validateTransactionCheckTime(messageBO.getTransactionCheckTime());

        validateBodySize(messageBO);
        validatePropertyCount(messageBO);
        validatePropertySize(messageBO);
    }

    public void validateTopic(String topicName) {
        TopicValidator.validateTopic(topicName);
    }

    private void validateBodySize(MessageBO messageBO) {
        int maxBodySize = messageConfig.getMaxBodySize();
        if (maxBodySize <= 0) {
            return;
        }

        if (messageBO.getBody().length > maxBodySize) {
            throw new InvalidRequestException(
                InvalidCode.MESSAGE_BODY_TOO_LARGE,
                "message body size is larger than " + maxBodySize
            );
        }
    }

    private void validatePropertySize(MessageBO messageBO) {
        int maxPropertySize = messageConfig.getMaxPropertySize();
        if (maxPropertySize <= 0) {
            return;
        }

        int propertySize = MapUtil.calculateLength(messageBO.getProperties());
        if (propertySize > maxPropertySize) {
            throw new InvalidRequestException(
                InvalidCode.MESSAGE_PROPERTIES_TOO_LARGE,
                "message properties size is larger than " + maxPropertySize
            );
        }
    }

    private void validatePropertyCount(MessageBO messageBO) {
        int maxPropertyCount = messageConfig.getMaxPropertyCount();
        if (maxPropertyCount <= 0) {
            return;
        }

        if (messageBO.getProperties().size() > maxPropertyCount) {
            throw new InvalidRequestException(
                InvalidCode.MESSAGE_PROPERTIES_TOO_LARGE,
                "message properties number is more than " + maxPropertyCount
            );
        }

    }

    private void validateShardingKey(String keyName) {
        if (StringUtil.isEmpty(keyName)) {
            return;
        }

        if (StringUtil.isBlank(keyName)) {
            throw new InvalidRequestException(InvalidCode.ILLEGAL_MESSAGE_GROUP, "message groupName can't be blank");
        }

        if (containControlCharacter(keyName)) {
            throw new InvalidRequestException(InvalidCode.ILLEGAL_MESSAGE_GROUP, "message groupName can't contain control character");
        }

        int maxLength = messageConfig.getMaxMessageGroupSize();
        if (maxLength <= 0) {
            return;
        }

        int groupLength = keyName.getBytes(MQConstants.MQ_CHARSET).length;
        if (groupLength >= maxLength) {
            throw new InvalidRequestException(InvalidCode.ILLEGAL_MESSAGE_GROUP, "message groupName can't be longer than " + maxLength);
        }
    }

    private void validateDeliverTime(long delayTime) {
        if (delayTime <= 0)  {
            return;
        }

        long maxDelayTime = brokerConfig.getTimerConfig().getMaxDelayTime();
        if (maxDelayTime <= 0) {
            return;
        }

        long now = System.currentTimeMillis();
        if (delayTime - now > maxDelayTime) {
            throw new InvalidRequestException(InvalidCode.ILLEGAL_DELIVERY_TIME, "message delay time is too large");
        }
    }

    private void validateTransactionCheckTime(long recoveryTime) {
        if (recoveryTime <= 0) {
            return;
        }

        long maxRecoveryTime = messageConfig.getMaxTransactionRecoverySecond();
        if (maxRecoveryTime <= 0) {
            return;
        }

        if (recoveryTime > maxRecoveryTime) {
            throw new InvalidRequestException(InvalidCode.BAD_REQUEST, "message transaction recovery time is too large");
        }
    }

    public void validateTag(String tag) {
        if (StringUtil.isEmpty(tag)) {
            return;

        }

        if (StringUtil.isBlank(tag)) {
            throw new InvalidRequestException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot be the char sequence of whitespace");
        }
        if (tag.contains("|")) {
            throw new InvalidRequestException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot contain '|'");
        }
        if (containControlCharacter(tag)) {
            throw new InvalidRequestException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot contain control character");
        }
    }

}
