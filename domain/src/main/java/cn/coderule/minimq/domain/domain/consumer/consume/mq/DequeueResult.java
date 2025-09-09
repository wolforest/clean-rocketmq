package cn.coderule.minimq.domain.domain.consumer.consume.mq;

import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
@ToString
@AllArgsConstructor
public class DequeueResult implements Serializable {
    private MessageStatus status;
    private List<MessageBO> messageList;

    private long nextOffset;
    private long minOffset;
    private long maxOffset;

    public DequeueResult() {
        this.status = MessageStatus.NO_MATCHED_MESSAGE;
        this.messageList = new ArrayList<>();
    }

    public void setStatusByMessageList() {
        this.status = messageList.isEmpty()
            ? MessageStatus.NO_MATCHED_MESSAGE
            : MessageStatus.FOUND;
    }

    public boolean isEmpty() {
        return messageList.isEmpty();
    }

    public int countMessage() {
        return messageList.size();
    }

    public MessageBO getMessage() {
        return getFirstMessage();
    }

    public MessageBO getFirstMessage() {
        return messageList.isEmpty()
            ? null
            : messageList.get(0);
    }

    public MessageBO getLastMessage() {
        return messageList.isEmpty()
            ? null
            : messageList.get(messageList.size() - 1);
    }

    public boolean noNewMessage() {
        return MessageStatus.OFFSET_OVERFLOW_ONE.equals(status);
    }

    public List<Long> getOffsetList() {
        return messageList.stream()
            .map(MessageBO::getQueueOffset)
            .collect(Collectors.toList());
    }

    public boolean isOffsetIllegal() {
        return MessageStatus.OFFSET_FOUND_NULL.equals(status)
            || MessageStatus.OFFSET_OVERFLOW_BADLY.equals(status)
            || MessageStatus.OFFSET_TOO_SMALL.equals(status)
            ;
    }

    public boolean hasNextOffset() {
        return nextOffset > 0;
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

    public static DequeueResult unknownError(Throwable t) {
        log.error("dequeue error", t);
        return DequeueResult.builder()
            .status(MessageStatus.UNKNOWN_ERROR)
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

