package cn.coderule.minimq.domain.domain.core.exception;

import cn.coderule.minimq.domain.domain.core.enums.code.BrokerExceptionCode;

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
