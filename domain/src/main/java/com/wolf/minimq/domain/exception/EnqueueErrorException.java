package com.wolf.minimq.domain.exception;

import com.wolf.common.lang.exception.BusinessException;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import lombok.Getter;

@Getter
public class EnqueueErrorException extends BusinessException {
    private EnqueueStatus status = EnqueueStatus.UNKNOWN_ERROR;

    public EnqueueErrorException(EnqueueStatus status) {
        this.status = status;
    }
}
