package cn.coderule.minimq.broker.api.validator;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.domain.enums.InvalidCode;
import cn.coderule.minimq.domain.domain.exception.InvalidParameterException;
import cn.coderule.minimq.domain.utils.topic.TopicValidator;

public class MetaValidator {
    private final TopicConfig topicConfig;

    public MetaValidator(TopicConfig topicConfig) {
        this.topicConfig = topicConfig;
    }

    public void validateTopic(String topicName) {
        if (StringUtil.isBlank(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is blank");
        }

        if (topicName.length() > topicConfig.getMaxTopicLength()) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is too long");
        }

        if (TopicValidator.isTopicOrGroupIllegal(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is illegal");
        }

        if (TopicValidator.isSystemTopic(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is system topic");
        }
    }
}
