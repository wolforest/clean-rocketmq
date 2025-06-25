package cn.coderule.minimq.broker.api.validator;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidParameterException;

import static cn.coderule.minimq.domain.domain.meta.topic.TopicValidator.isTopicOrGroupIllegal;

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
    }
}
