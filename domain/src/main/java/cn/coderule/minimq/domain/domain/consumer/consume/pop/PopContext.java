package cn.coderule.minimq.domain.domain.consumer.consume.pop;

import cn.coderule.minimq.domain.domain.MessageQueue;
import java.io.Serializable;
import lombok.Data;

@Data
public class PopContext implements Serializable {
    private final long popTime;
    private final PopRequest popRequest;
    private final MessageQueue messageQueue;

    private int reviveQueueId;

    public PopContext(PopRequest popRequest, MessageQueue messageQueue) {
        this.popRequest = popRequest;
        this.messageQueue = messageQueue;

        this.popTime = System.currentTimeMillis();
    }

}
