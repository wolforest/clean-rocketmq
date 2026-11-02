package com.wolfmq.broker.api.validator;

import com.wolfmq.common.config.server.BrokerConfig;
import com.wolfmq.common.core.constant.code.InvalidCode;
import com.wolfmq.common.core.exception.InvalidParameterException;

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
