package cn.coderule.minimq.broker.api.validator;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;

public class PopValidator {
    private final BrokerConfig brokerConfig;
    private final MessageConfig messageConfig;

    public PopValidator(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.messageConfig = brokerConfig.getMessageConfig();
    }

    public void validate(PopRequest request) {

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
}
