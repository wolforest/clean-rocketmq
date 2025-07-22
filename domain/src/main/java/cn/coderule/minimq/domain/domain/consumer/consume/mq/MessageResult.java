package cn.coderule.minimq.domain.domain.consumer.consume.mq;

import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class MessageResult implements Serializable {
    private MessageStatus status;
    private MessageBO message;

    public MessageResult() {
        this.status = MessageStatus.NO_MATCHED_MESSAGE;
    }

    public boolean isSuccess() {
        return null != message;
    }

    public boolean isOffsetIllegal() {
        return MessageStatus.OFFSET_FOUND_NULL.equals(status)
            || MessageStatus.OFFSET_OVERFLOW_BADLY.equals(status)
            || MessageStatus.OFFSET_TOO_SMALL.equals(status)
            || MessageStatus.OFFSET_RESET.equals(status)
            ;
    }

    public static MessageResult success(MessageBO message) {
        return MessageResult.builder()
            .status(MessageStatus.FOUND)
            .message(message)
            .build();
    }

    public static MessageResult notFound() {
        return MessageResult.builder()
            .status(MessageStatus.NO_MATCHED_MESSAGE)
            .message(null)
            .build();
    }

}

