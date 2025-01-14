package com.wolf.minimq.domain.model.dto;

import com.wolf.minimq.domain.enums.MessageStatus;
import com.wolf.minimq.domain.model.bo.MessageBO;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class GetResult implements Serializable {
    private MessageStatus status;
    private List<MessageBO> messageList;
}

