package cn.coderule.minimq.domain.domain.dto;

import cn.coderule.minimq.domain.domain.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
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

    public DequeueResult() {
        this.status = MessageStatus.NO_MATCHED_MESSAGE;
        this.messageList = new ArrayList<>();
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

