package cn.coderule.minimq.rpc.common.grpc.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import cn.coderule.minimq.domain.domain.enums.code.InvalidCode;
import lombok.Getter;

@Getter
public class GrpcException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Invalid parameter";
    private final InvalidCode invalidCode;

    public GrpcException(InvalidCode code) {
        this(code, DEFAULT_MESSAGE);
    }

    public GrpcException(InvalidCode code, String message) {
        this(code, message, null);
    }

    public GrpcException(InvalidCode code, String message, Throwable t) {
        super(500, message, t);
        this.invalidCode = code;
    }
}
