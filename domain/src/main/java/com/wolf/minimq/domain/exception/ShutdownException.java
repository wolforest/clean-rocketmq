package com.wolf.minimq.domain.exception;

import com.wolf.common.lang.exception.SystemException;
import lombok.Getter;

@Getter
public class ShutdownException extends SystemException {

    public ShutdownException(String msg) {
        super("Shutdown failed: " + msg);
    }
}
