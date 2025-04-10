package cn.coderule.minimq.domain.domain.exception;

import cn.coderule.common.lang.exception.BusinessException;
import lombok.Getter;

@Getter
public class MQException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "MQ exception";
    private final String address;

    public MQException(int code) {
        this(code, DEFAULT_MESSAGE);
    }

    public MQException(int code, String message) {
        this(code, message, null);
    }

    public MQException(int code, String message, String address) {
        super(code, message);
        this.address = address;
    }
}
