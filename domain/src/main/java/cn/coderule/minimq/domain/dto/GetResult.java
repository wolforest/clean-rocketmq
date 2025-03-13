package cn.coderule.minimq.domain.dto;

import cn.coderule.minimq.domain.enums.MessageStatus;
import cn.coderule.minimq.domain.model.message.MessageBO;
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
public class GetResult implements Serializable {
    private MessageStatus status;
    private List<MessageBO> messageList;

    public GetResult() {
        this.status = MessageStatus.NO_MATCHED_MESSAGE;
        this.messageList = new ArrayList<>();
    }

    public void addMessage(@NonNull MessageBO messageBO) {
        status = messageBO.getStatus();
        messageList.add(messageBO);
    }

    public static GetResult success(List<MessageBO> messageList) {
        return GetResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(messageList)
            .build();
    }

    public static GetResult notFound() {
        return GetResult.builder()
            .status(MessageStatus.NO_MATCHED_MESSAGE)
            .messageList(List.of())
            .build();
    }
}

