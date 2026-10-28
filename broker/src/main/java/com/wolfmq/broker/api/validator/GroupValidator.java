package com.wolfmq.broker.api.validator;

import cn.coderule.common.util.lang.string.StringUtil;
import com.wolfmq.domain.core.constant.MQConstants;
import com.wolfmq.domain.core.constant.code.InvalidCode;
import com.wolfmq.domain.core.exception.InvalidParameterException;

import static com.wolfmq.domain.domain.meta.topic.TopicValidator.isTopicOrGroupIllegal;

public class GroupValidator {
    public static final int CHARACTER_MAX_LENGTH = 255;

    public static void validate(String group) throws InvalidParameterException {
        if (StringUtil.isBlank(group)) {
            throw new InvalidParameterException(
                InvalidCode.ILLEGAL_CONSUMER_GROUP,
                "the specified group is blank"
            );
        }

        if (group.length() > CHARACTER_MAX_LENGTH) {
            throw new InvalidParameterException(
                InvalidCode.ILLEGAL_CONSUMER_GROUP,
                "the specified group is longer than group max length 255."
            );
        }


        if (isTopicOrGroupIllegal(group)) {
            throw new InvalidParameterException(
                InvalidCode.ILLEGAL_CONSUMER_GROUP,
                String.format(
                    "the specified group[%s] contains illegal characters, allowing only %s",
                    group, "^[%|a-zA-Z0-9_-]+$"
                )
            );
        }

        if (MQConstants.isSysConsumerGroup(group)) {
            throw new InvalidParameterException(
                InvalidCode.ILLEGAL_CONSUMER_GROUP,
                "the specified group is a system group."
            );
        }
    }
}
