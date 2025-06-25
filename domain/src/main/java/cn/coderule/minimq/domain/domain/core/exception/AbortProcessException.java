package cn.coderule.minimq.domain.domain.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import cn.coderule.minimq.domain.domain.core.enums.code.InvalidCode;

public class AbortProcessException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Abort process";
    private final InvalidCode invalidCode;

    public AbortProcessException(InvalidCode code) {
        this(code, DEFAULT_MESSAGE);
    }

    public AbortProcessException(InvalidCode code, String message) {
        super(code.getCode(), message);
        this.invalidCode = code;
    }
}
