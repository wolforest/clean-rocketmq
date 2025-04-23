package cn.coderule.minimq.broker.api.validator;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;

public class MessageValidator {
    private final MessageConfig messageConfig;

    public MessageValidator(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    public void validate(MessageBO messageBO) {

    }
}
