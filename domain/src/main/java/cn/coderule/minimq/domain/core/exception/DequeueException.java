package cn.coderule.minimq.domain.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
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
