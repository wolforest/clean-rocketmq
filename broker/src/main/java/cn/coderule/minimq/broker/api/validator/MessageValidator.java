package cn.coderule.minimq.broker.api.validator;

import apache.rocketmq.v2.Code;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.enums.InvalidCode;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.rpc.common.grpc.core.exception.GrpcException;
import com.google.common.base.CharMatcher;

public class MessageValidator {
    private final MessageConfig messageConfig;

    public MessageValidator(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    public void validate(MessageBO messageBO) {

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
