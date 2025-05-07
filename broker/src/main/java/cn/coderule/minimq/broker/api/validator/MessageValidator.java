package cn.coderule.minimq.broker.api.validator;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.utils.topic.TopicValidator;
import cn.coderule.minimq.rpc.common.grpc.core.exception.GrpcException;
import com.google.common.base.CharMatcher;

public class MessageValidator {
    private final MessageConfig messageConfig;

    public MessageValidator(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    public void validate(MessageBO messageBO) {

    }

    private void validateTopic(String topicName) {
        TopicValidator.validateTopic(topicName);
    }

    private void validateProperty() {
        // num, size
    }

    private void validateMessageGroup() {

    }

    private void validateMessageKey() {

    }

    private void validateDelayTime() {

    }

    private void validateTransactionRecoveryTime() {

    }

    public void validateTag(String tag) {
        if (StringUtil.isEmpty(tag)) {
            return;

        }

        if (StringUtil.isBlank(tag)) {
            throw new GrpcException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot be the char sequence of whitespace");
        }
        if (tag.contains("|")) {
            throw new GrpcException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot contain '|'");
        }
        if (containControlCharacter(tag)) {
            throw new GrpcException(InvalidCode.ILLEGAL_MESSAGE_TAG, "tag cannot contain control character");
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
