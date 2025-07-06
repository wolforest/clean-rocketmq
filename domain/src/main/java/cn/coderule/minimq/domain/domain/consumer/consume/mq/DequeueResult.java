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
public class DequeueResult implements Serializable {
    private MessageStatus status;
    private List<MessageBO> messageList;

    private long minOffset;
    private long maxOffset;
    private long nextOffset;

    public DequeueResult() {
        this.status = MessageStatus.NO_MATCHED_MESSAGE;
        this.messageList = new ArrayList<>();
    }

    public boolean isEmpty() {
        return messageList.isEmpty();
    }

    public MessageBO getMessage() {
        return messageList.isEmpty()
            ? null
            : messageList.get(0);
    }

    public boolean hasNewMessage() {
        return !MessageStatus.OFFSET_OVERFLOW_ONE.equals(status);
    }

    public boolean isOffsetIllegal() {
        return MessageStatus.OFFSET_FOUND_NULL.equals(status)
            || MessageStatus.OFFSET_OVERFLOW_BADLY.equals(status)
            || MessageStatus.OFFSET_TOO_SMALL.equals(status)
            || MessageStatus.OFFSET_RESET.equals(status)
            ;
    }

    public void addMessage(@NonNull MessageBO messageBO) {
        status = messageBO.getStatus();
        messageList.add(messageBO);
    }

    public static DequeueResult success(List<MessageBO> messageList) {
        return DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(messageList)
            .build();
    }

    public static DequeueResult notFound() {
        return DequeueResult.builder()
            .status(MessageStatus.NO_MATCHED_MESSAGE)
            .messageList(List.of())
            .build();
    }

    public static DequeueResult lockFailed() {
        return DequeueResult.builder()
            .status(MessageStatus.LOCK_FAILED)
            .messageList(List.of())
            .build();
    }
}

