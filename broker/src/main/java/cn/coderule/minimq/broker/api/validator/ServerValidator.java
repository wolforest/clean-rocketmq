package cn.coderule.minimq.broker.api.validator;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidParameterException;

public class ServerValidator {
    private final BrokerConfig brokerConfig;

    public ServerValidator(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
    }

    public void checkServerReady() {
        long serverReadyTime = brokerConfig.getServerReadyTime();
        if (serverReadyTime <= 0) {
            return;
        }

        if (System.currentTimeMillis() < serverReadyTime) {
            throw new InvalidParameterException(InvalidCode.INTERNAL_ERROR, "server is not ready");
        }
    }
}
