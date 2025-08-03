package cn.coderule.minimq.broker.api.validator;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;
import cn.coderule.minimq.rpc.common.core.exception.RequestException;
import com.google.common.base.CharMatcher;

public class MessageValidator {
    private final MessageConfig messageConfig;

    public MessageValidator(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    public void validate(MessageBO messageBO) {

    }

    public void validateTopic(String topicName) {
        TopicValidator.validateTopic(topicName);
    }

    private void validateProperty() {
        // num, size
    }

    private void validateMessageGroup(String groupName) {
        if (StringUtil.isEmpty(groupName)) {
            return;
        }

        if (StringUtil.isBlank(groupName)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_GROUP, "message groupName can't be blank");
        }

        if (containControlCharacter(groupName)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_GROUP, "message groupName can't contain control character");
        }

        int maxLength = messageConfig.getMaxMessageGroupSize();
        if (maxLength <= 0) {
            return;
        }

        int groupLength = groupName.getBytes(MQConstants.MQ_CHARSET).length;
        if (groupLength >= maxLength) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_GROUP, "message groupName can't be longer than " + maxLength);
        }
    }

    private void validateMessageKey(String key) {
        if (StringUtil.isEmpty(key)) {
            return;
        }

        if (StringUtil.isBlank(key)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_KEY, "message can't be blank");
        }

        if (containControlCharacter(key)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_KEY, "message key can't contain control character");
        }
    }

    private void validateDelayTime(long delayTime) {
        long maxDelayTime = messageConfig.getMaxDelayTimeMills();
        if (maxDelayTime <= 0) {
            return;
        }

        long now = System.currentTimeMillis();
        if (delayTime - now > maxDelayTime) {
            throw new RequestException(InvalidCode.ILLEGAL_DELIVERY_TIME, "message delay time is too large");
        }
    }

    private void validateTransactionRecoveryTime() {

    }

    public void validateTag(String tag) {
        if (StringUtil.isEmpty(tag)) {
            return;

        }

        if (StringUtil.isBlank(tag)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot be the char sequence of whitespace");
        }
        if (tag.contains("|")) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot contain '|'");
        }
        if (containControlCharacter(tag)) {
            throw new RequestException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot contain control character");
        }
    }

    public boolean containControlCharacter(String data) {
        for (int i = 0; i < data.length(); i++) {
            if (CharMatcher.javaIsoControl().matches(data.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
