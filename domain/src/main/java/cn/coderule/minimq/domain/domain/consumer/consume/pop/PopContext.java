package cn.coderule.minimq.domain.domain.consumer.consume.pop;

import cn.coderule.minimq.domain.domain.MessageQueue;
import java.io.Serializable;
import lombok.Data;

@Data
public class PopContext implements Serializable {
    private PopRequest popRequest;
    private MessageQueue messageQueue;

    public PopContext(PopRequest popRequest, MessageQueue messageQueue) {
        this.popRequest = popRequest;
        this.messageQueue = messageQueue;
    }

}
