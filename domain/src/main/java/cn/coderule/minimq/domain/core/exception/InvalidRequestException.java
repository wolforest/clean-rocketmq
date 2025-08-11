package cn.coderule.minimq.domain.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import lombok.Getter;

@Getter
public class InvalidRequestException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Invalid parameter";
    private final InvalidCode invalidCode;

    public InvalidRequestException(InvalidCode code) {
        this(code, DEFAULT_MESSAGE);
    }

    public InvalidRequestException(InvalidCode code, String message) {
        this(code, message, null);
    }

    public InvalidRequestException(InvalidCode code, String message, Throwable t) {
        super(500, message, t);
        this.invalidCode = code;
    }
}
