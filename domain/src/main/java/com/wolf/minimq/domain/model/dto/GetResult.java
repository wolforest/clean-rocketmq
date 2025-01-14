package com.wolf.minimq.domain.model.dto;

import com.wolf.minimq.domain.enums.MessageStatus;
import com.wolf.minimq.domain.model.bo.MessageBO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetResult implements Serializable {
    private MessageStatus status;
    private List<MessageBO> messageList;

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

    public static GetResult newInstance() {
        return GetResult.builder()
            .status(MessageStatus.NO_MATCHED_MESSAGE)
            .messageList(new ArrayList<>())
            .build();
    }

    public void addMessage(@NonNull MessageBO messageBO) {
        status = messageBO.getStatus();
        messageList.add(messageBO);
    }
}

