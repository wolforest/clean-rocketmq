package com.wolf.minimq.domain.context;

import com.wolf.minimq.domain.model.Message;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class MessageContext implements Serializable {
    private List<Message> messageList;
}
