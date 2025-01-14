package com.wolf.minimq.domain.exception;

import com.wolf.common.lang.exception.BusinessException;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import lombok.Getter;

@Getter
public class EnqueueException extends BusinessException {
    private EnqueueStatus status = EnqueueStatus.UNKNOWN_ERROR;

    public EnqueueException(EnqueueStatus status) {
        this.status = status;
    }
}
