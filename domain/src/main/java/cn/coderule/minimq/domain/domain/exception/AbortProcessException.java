package cn.coderule.minimq.domain.domain.exception;

import cn.coderule.common.lang.exception.BusinessException;

public class AbortProcessException extends BusinessException {
    private static final String DEFAULT_MESSAGE = "Abort process";

    public AbortProcessException(int code) {
        this(code, DEFAULT_MESSAGE);
    }

    public AbortProcessException(int code, String message) {
        super(code, message);
    }
}
