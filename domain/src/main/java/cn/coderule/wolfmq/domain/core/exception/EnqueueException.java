package cn.coderule.wolfmq.domain.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import cn.coderule.wolfmq.domain.domain.mq.dto.EnqueueStatus;
import lombok.Getter;

@Getter
public class EnqueueException extends BusinessException {
    private EnqueueStatus status = EnqueueStatus.UNKNOWN_ERROR;

    public EnqueueException(EnqueueStatus status) {
        this.status = status;
    }
}
