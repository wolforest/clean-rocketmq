package cn.coderule.minimq.domain.domain.exception;

import cn.coderule.common.lang.exception.BusinessException;
import cn.coderule.minimq.domain.domain.enums.code.InvalidCode;
import lombok.Getter;

@Getter
public class InvalidParameterException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Invalid parameter";
    private final InvalidCode invalidCode;

    public InvalidParameterException(InvalidCode code) {
        this(code, DEFAULT_MESSAGE);
    }

    public InvalidParameterException(InvalidCode code, String message) {
        super(500, message);
        this.invalidCode = code;
    }
}
