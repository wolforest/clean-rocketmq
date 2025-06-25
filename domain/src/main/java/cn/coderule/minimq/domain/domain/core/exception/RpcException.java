package cn.coderule.minimq.domain.domain.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import lombok.Getter;

@Getter
public class RpcException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "MQ exception";
    private final String address;

    public RpcException(int code) {
        this(code, DEFAULT_MESSAGE);
    }

    public RpcException(int code, String message) {
        this(code, message, null);
    }

    public RpcException(int code, String message, String address) {
        super(code, message);
        this.address = address;
    }
}
