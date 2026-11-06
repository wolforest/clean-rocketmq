package com.wolfmq.common.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueStatus;
import lombok.Getter;

@Getter
public class EnqueueException extends BusinessException {
    private EnqueueStatus status = EnqueueStatus.UNKNOWN_ERROR;

    public EnqueueException(EnqueueStatus status) {
        this.status = status;
    }
}
