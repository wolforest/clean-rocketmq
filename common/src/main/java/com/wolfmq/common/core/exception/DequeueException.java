package com.wolfmq.common.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import com.wolfmq.common.domain.message.enums.MessageStatus;
import com.wolfmq.common.domain.messagequeue.dto.DequeueResult;
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
