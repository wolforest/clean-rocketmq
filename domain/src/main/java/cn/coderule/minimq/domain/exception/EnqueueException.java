package cn.coderule.minimq.domain.exception;

import cn.coderule.common.lang.exception.BusinessException;
import cn.coderule.minimq.domain.enums.EnqueueStatus;
import lombok.Getter;

@Getter
public class EnqueueException extends BusinessException {
    private EnqueueStatus status = EnqueueStatus.UNKNOWN_ERROR;

    public EnqueueException(EnqueueStatus status) {
        this.status = status;
    }
}
