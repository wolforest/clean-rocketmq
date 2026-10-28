package com.wolfmq.domain.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import com.wolfmq.domain.domain.message.enums.MessageStatus;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import java.util.List;
import lombok.Getter;

@Getter
public class DequeueException extends BusinessException {
    private MessageStatus status = MessageStatus.UNKNOWN_ERROR;

    public DequeueException(MessageStatus status) {
        this.status = status;
    }

    public DequeueResult toResult() {
        return DequeueResult.builder()
            .status(status)
            .messageList(List.of())
            .build();
    }
}
