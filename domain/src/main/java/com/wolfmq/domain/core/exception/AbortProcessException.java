package com.wolfmq.domain.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import com.wolfmq.domain.core.constant.code.InvalidCode;

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
