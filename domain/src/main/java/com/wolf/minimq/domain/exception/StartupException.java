package com.wolf.minimq.domain.exception;

import com.wolf.common.lang.exception.SystemException;
import lombok.Getter;

@Getter
public class StartupException extends SystemException {

    public StartupException(String msg) {
        super("Start failed: " + msg);
    }
}
