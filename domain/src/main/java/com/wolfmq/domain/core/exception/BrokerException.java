package com.wolfmq.domain.core.exception;

import com.wolfmq.domain.core.constant.code.BrokerExceptionCode;

public class BrokerException extends RuntimeException {

    private final BrokerExceptionCode code;

    public BrokerException(BrokerExceptionCode code, String message) {
        super(message);
        this.code = code;
    }

    public BrokerException(BrokerExceptionCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public BrokerExceptionCode getCode() {
        return code;
    }
}
